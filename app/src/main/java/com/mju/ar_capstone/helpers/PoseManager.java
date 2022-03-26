package com.mju.ar_capstone.helpers;

import android.location.Location;
import android.util.Log;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Vector3;
import com.mju.ar_capstone.ArSfActivity;

public class PoseManager {

    public static int TO_GRID = 0;
    public static int TO_GPS = 1;
    private final static double SCALE = 10;

    public PoseManager() {
        //기본 생성자
    }

    class LatXLngY
    {
        public double lat;
        public double lng;

        public double x;
        public double y;

    }

    // 둘 방위각 차이만큼 벡터를 회전시켜줄꺼임
    // 어느 방향인지까지 고려해서 리턴
    public int azimuthDifference(int myAzimuth, int anchorAzimuth){

        int azimuthDirection = Math.abs(myAzimuth - anchorAzimuth);

        Log.d("차이 azimuth My", String.valueOf(myAzimuth));
        Log.d("차이 azimuth Anchor", String.valueOf(anchorAzimuth));


        //나의 방위각이 앵커 방위각 보다 크면 그 차이만큼
        // 음의 방향으로 회전해줘야함
        // 반대는 양의 방향으로 회전
        // 여기까지는 일반 방위각 기준으로 생각함
        if(myAzimuth > anchorAzimuth){
            azimuthDirection = -azimuthDirection;
        }else {
            azimuthDirection = azimuthDirection;
        }

        return azimuthDirection;
    }

    //벡터 회전
    public Vector3 vertorRotate(Vector3 vector3, int degree){
        Vector3 rotatedVector = new Vector3(
                (float) (vector3.x * Math.cos(degree) - vector3.z * Math.sin(degree)),
                vector3.y,
                (float) (vector3.x * Math.sin(degree) + vector3.z * Math.cos(degree))
        );

        return rotatedVector;
    }


    // 방위각에 따라 돌리는 코드는 이상이 없음
    // 문제가 있다면 방위각을 가져오는 소스코드 문제일듯
    public Pose resolveRealPose(Pose pose, int degree){ //좌표간 거리는 잠시 나중에 처리하자

        float[] tmpT = pose.getTranslation();
        float[] tmpR = pose.getRotationQuaternion();

        //불러온 포즈를 가지고 벡터를 만들음
        Vector3 vector3 = new Vector3(
                tmpT[0],
                tmpT[1],
                tmpT[2]
        );

        Vector3 rotatedVector = vertorRotate(vector3, degree);

        Pose resolvePose = new Pose(
                new float[]{rotatedVector.x, rotatedVector.y, rotatedVector.z},
                tmpR
        );
        return resolvePose;
    }


    public double[] distanceBetweenLocation(Location user, Location anchor){

        double[] distanceArray = {0.0, 0.0, 0.0};
        double distance = 0.0; //두 지점 사이의 거리
        double distanceX = 0.0; //x끼리 차이
        double distanceY = 0.0; //y끼리 차이

        LatXLngY userXY = convertGRID_GPS(TO_GRID, user.getLatitude(), user.getLongitude());
        LatXLngY anchorXY = convertGRID_GPS(TO_GRID, anchor.getLatitude(), anchor.getLongitude());

        Log.d("거리 유저//", "x: " + userXY.x + ", y: " + userXY.y);
        Log.d("거리 앵커", "x: " + anchorXY.x + ", y: " + anchorXY.y);

        //우선 각각 차이 구함
        distanceX = Math.abs(userXY.x - anchorXY.x);
        distanceY = Math.abs(userXY.y - anchorXY.y);

        if(userXY.x < anchorXY.x){
            distanceX = distanceX;
        }
        if(userXY.y < anchorXY.y){
            distanceY = -distanceY;
        }

        Log.d("거리 x 차이", String.valueOf(distanceX));
        Log.d("거리 y 차이", String.valueOf(distanceY));

        distance = user.distanceTo(anchor);
        Log.d("거리 나랑 앵커", String.valueOf(distance));

        distanceArray[0] = distance;
        distanceArray[1] = distanceX * SCALE;
        distanceArray[2] = distanceY * SCALE;

        return distanceArray;
    }

    //좌표 부분
    public LatXLngY convertGRID_GPS(int mode, double lat_X, double lng_Y )
    {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
        //

        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        LatXLngY rs = new LatXLngY();

        if (mode == TO_GRID) {
            rs.lat = lat_X;
            rs.lng = lng_Y;
            double ra = Math.tan(Math.PI * 0.25 + (lat_X) * DEGRAD * 0.5);
            ra = re * sf / Math.pow(ra, sn);
            double theta = lng_Y * DEGRAD - olon;
            if (theta > Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;
            theta *= sn;
            rs.x = ra * Math.sin(theta) + XO + 0.5;
            rs.y = ro - ra * Math.cos(theta) + YO + 0.5;
        }
        else {
            rs.x = lat_X;
            rs.y = lng_Y;
            double xn = lat_X - XO;
            double yn = ro - lng_Y + YO;
            double ra = Math.sqrt(xn * xn + yn * yn);
            if (sn < 0.0) {
                ra = -ra;
            }
            double alat = Math.pow((re * sf / ra), (1.0 / sn));
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

            double theta = 0.0;
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            }
            else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5;
                    if (xn < 0.0) {
                        theta = -theta;
                    }
                }
                else theta = Math.atan2(xn, yn);
            }
            double alon = theta / sn + olon;
            rs.lat = alat * RADDEG;
            rs.lng = alon * RADDEG;
        }
        return rs;
    }


}
