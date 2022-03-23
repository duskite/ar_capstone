package com.mju.ar_capstone.helpers;

import android.location.Location;
import android.util.Log;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Vector3;
import com.mju.ar_capstone.ArSfActivity;

public class PoseManager {

    public static int TO_GRID = 0;
    public static int TO_GPS = 1;
    private final static double SCALE = 1000.0;
    private final static int DEGREE_CONTROL = 15;

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

    //앵커가 남겨졌던 loadedAzimuth로 북쪽 벡터를 구함
    public Vector3 createNorthVector(Vector3 vector3, int loadedAzimuth){
        //앵커가 찍혔던 방위각만큼 다시 반대로 돌리면 정북방향 앵커가 나옴
        return vector3roatate(vector3, -loadedAzimuth);
    }


    // 방위각에 따라 돌리는 코드는 이상이 없음
    // 문제가 있다면 방위각을 가져오는 소스코드 문제일듯
    public Pose makeRealPosePosition(Pose pose, double[] distanceArray, int loadedAzimuth, int myAzimuth){

        Log.d("앵커위치", "방위각 불러온거" + loadedAzimuth);
        Log.d("앵커위치", "방위각 내꺼" + myAzimuth);
        float[] tmp = pose.getTranslation();

        //불러온 포즈를 가지고 벡터를 만들음
        Vector3 vectorOld = new Vector3(
                tmp[0],
                tmp[1],
                tmp[2]
        );
        int tmpAzimuth = 0;
        // 식 자체는 논리적으로 문제없음. 근데 지표면 인식에 따라서 결과가 많이 상이함
        if(myAzimuth > loadedAzimuth){
            tmpAzimuth = -Math.abs(myAzimuth - loadedAzimuth); // 맞음
        }else {
            tmpAzimuth = Math.abs(myAzimuth - loadedAzimuth); // 맞음
        }
        Log.d("앵커위치", "방위각 차이" + tmpAzimuth);

        //방위각에 따라 어느쪽으로 돌릴지 결정해야함
        Vector3 vectorNew = vector3roatate(vectorOld, tmpAzimuth);
        // 이미 한번 내가 보는 방향에서 원래 앵커가남겨졌던 방향으로 회전이 된거임
        // 이 앵커를 다시 정북 방향으로 보냄
        Vector3 vectorNorth = createNorthVector(vectorNew, loadedAzimuth);
        // 새로 만든 벡터와 북쪽벡터, x축 변화량을 보냄
        computeXVector(vectorNew, vectorNorth, distanceArray[1]);

        return Pose.makeTranslation(vectorNew.x, vectorNew.y, vectorNew.z);
    }

    //근데 여기서 distanceX를 얼마만큼 scale up할껀지가 중요함
    public void computeXVector(Vector3 vectorNew, Vector3 vectorNorth, double distanceX){
        Vector3 vectorC = Vector3.subtract(vectorNorth, vectorNew);
    }

    //벡터를 각도만큼 회전한 후 리턴
    public Vector3 vector3roatate(Vector3 vector3, int degree){

        Double radian = Math.toRadians(degree);
        Vector3 vector3rotated = new Vector3(
                ((float) (vector3.x * Math.cos(radian)) - (float) (vector3.z * Math.sin(radian))),
                vector3.y,
                ((float) (vector3.x * Math.sin(radian)) + (float) (vector3.z * Math.cos(radian)))
        );
        return vector3rotated;
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

        // 부호 바꿔줌
        // 여기서는 일반적인 좌표로 생각함
        if(userXY.x > anchorXY.x){
            distanceX = -distanceX;
        }
        if(userXY.y < anchorXY.y){
            distanceY = distanceY;
        }

        Log.d("거리 x 차이", String.valueOf(distanceX));
        Log.d("거리 y 차이", String.valueOf(distanceY));

//        distance = Math.sqrt(Math.pow((userXY.x - anchorXY.x),2) + Math.pow((userXY.y - anchorXY.y),2));
//        Log.d("거리: ", String.valueOf(distance));

        distance = user.distanceTo(anchor);

        distanceArray[0] = distance;
        distanceArray[1] = distanceX;
        distanceArray[2] = distanceY;

        //미터 단위로 바꿔서 넘겨줘야함
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
