package com.mju.ar_capstone.helpers;

import android.location.Location;
import android.util.Log;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Vector3;

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

        int azimuthDegree = Math.abs(myAzimuth - anchorAzimuth);

        Log.d("차이 azimuth My", String.valueOf(myAzimuth));
        Log.d("차이 azimuth Anchor", String.valueOf(anchorAzimuth));


        //나의 방위각이 앵커 방위각 보다 크면 그 차이만큼
        // 음의 방향으로 회전해줘야함
        // 반대는 양의 방향으로 회전
        if(myAzimuth > anchorAzimuth){
            azimuthDegree = -azimuthDegree;
        }

        return azimuthDegree;
    }

    public int vectorSection(Vector3 vector3){
        if(vector3.x > 0 && vector3.z < 0){ // +, - 앞 우측
            return 1;
        }else if(vector3.x <0 && vector3.z<0){ // -,- 앞 좌측
            return 2;
        }else if(vector3.x<0 && vector3.z>0){ // -, + 뒤 좌측
            return 3;
        }else if(vector3.x >0 && vector3.z>0){ // +, + 뒤 우측
            return 4;
        }

        //여기는 안오는거
        return 0;
    }

    public int[] computeAdjustDegree(int degree){
        int[] adjustDegree = new int[5];
        // 섹션과 디그리에 따라서 동적으로 조정해야할듯
        adjustDegree[1] = 0;
        adjustDegree[2] = 0;
        adjustDegree[3] = 0;
        adjustDegree[4] = 0;

        if(degree < -5){
            if(Math.abs(degree) < 100){
                adjustDegree[1] = 30;
                adjustDegree[2]= 30;
                adjustDegree[3] = 30;
                adjustDegree[4] = 30;
            }else if(Math.abs(degree) < 130){
                adjustDegree[1] = 15;
                adjustDegree[2] = 15;
                adjustDegree[3] = 30;
                adjustDegree[4] = 30;
            }else if(Math.abs(degree) < 160){
                adjustDegree[1] = 0;
                adjustDegree[2] = 10;
                adjustDegree[3] = 30;
                adjustDegree[4] = 10;
            }else if(Math.abs(degree) < 200){
                adjustDegree[1] = 0;
                adjustDegree[2] = 10;
                adjustDegree[3] = -10;
                adjustDegree[4] = -10;
            }else if(Math.abs(degree) < 230){
                adjustDegree[1] = -10;
                adjustDegree[2] = 10;
                adjustDegree[3] = 0;
                adjustDegree[4] = -20;
            }else if(Math.abs(degree) < 250){
                adjustDegree[1] = -10;
                adjustDegree[2] = 10;
                adjustDegree[3] = -20;
                adjustDegree[4] = -30;
            }else if(Math.abs(degree) < 300){
                adjustDegree[1] = 0;
                adjustDegree[2] = 10;
                adjustDegree[3] = -5;
                adjustDegree[4] = -30;
            }else if(Math.abs(degree) < 330){
                adjustDegree[1] = 0;
                adjustDegree[2] = 10;
                adjustDegree[3] = -5;
                adjustDegree[4] = -20;
            }else if(Math.abs(degree) < 360){
                adjustDegree[1] = 0;
                adjustDegree[2] = 10;
                adjustDegree[3] = 5;
                adjustDegree[4] = 0;
            }

            return adjustDegree;
        }else{

        }

        return adjustDegree;
    }


    //벡터 회전
    public Vector3 vertorRotate(Vector3 vector3, int degree){

        //각도 차이에 따라 x, z축 길이를 보정하고
        //벡터 회전을 진행함
        int section = vectorSection(vector3);

        if(Math.abs(degree) <= 10){ //방위각이 거의 일치할 경우
            //벡터 그대로 놔둠
        }else if(Math.abs(degree) >= 170 && Math.abs(degree) <= 190){ // 방위각이 180도, 정반대 일 경우
            //벡터 멀리만 보냄
            vector3.z = vector3.z - 0.6f;
        }
        else if(degree < -5){ // -임, 방위각 나 > 앵커, degree < 0
            // 각 사분면에 따라 다르게 조정할 각도 담고 있음
            int[] adjustDegree = computeAdjustDegree(degree);
            switch (section){
                case 1:
                    degree += adjustDegree[1];
                    vector3.x = vector3.x * 1.1f;
                    vector3.z = vector3.z * 1.1f;
                    break;
                case 2:
                    degree += adjustDegree[2];
                    vector3.x = vector3.x * 1.1f;
                    vector3.z = vector3.z * 1.1f;
                    break;
                case 3:
                    degree += adjustDegree[3];
                    vector3.x = vector3.x * 0.9f;
                    vector3.z = vector3.z * 0.9f;
                    break;
                case 4:
                    degree += adjustDegree[4];
                    vector3.x = vector3.x * 0.9f;
                    vector3.z = vector3.z * 0.9f;
                    break;
            }
//        }else { // 앵커 > 나
//            switch (section){
//                case 1:
//                case 2:
//                    // 더 회전 시킴
//                    degree += 5;
//                    vector3.x = vector3.x * 1.1f;
//                    vector3.z = vector3.z * 1.1f;
//                    break;
//                case 3:
//                case 4:
//                    //좀 덜 회전시킴
//                    degree -= 5;
//                    vector3.x = vector3.x * 0.9f;
//                    vector3.z = vector3.z * 0.9f;
//                    break;
//            }
//
        }

        Vector3 rotatedVector = new Vector3(
                (float) (vector3.x * Math.cos(Math.toRadians(degree)) - vector3.z * Math.sin(Math.toRadians(degree))),
                vector3.y,
                (float) (vector3.x * Math.sin(Math.toRadians(degree)) + vector3.z * Math.cos(Math.toRadians(degree)))
        );


        return rotatedVector;
    }


    public Pose resolveRealPose(Pose pose, int degree){ //좌표간 거리는 잠시 나중에 처리하자

        float[] tmpT;
        float[] tmpR;

        try {
            tmpT = pose.getTranslation();
            tmpR = pose.getRotationQuaternion();
        }catch (NullPointerException e){
            tmpT = new float[]{0, 0, 0};
            tmpR = new float[]{0, 0, 0, 0};
            Log.d("포즈 null", "null 뜸");
        }


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
