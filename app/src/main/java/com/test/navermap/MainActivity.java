// 메인 지도 액티비티
package com.test.navermap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Overlay.OnClickListener,OnMapReadyCallback, NaverMap.OnMapClickListener{
    private MapView mapView;
    private static NaverMap naverMap;
    ImageView star;
    TextView location_name;
    LinearLayout sub;

    //회원 아이디 값
    String id="test";

    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    // DB 경로
    private DocumentReference documentReference=firebaseFirestore.collection("users").document(id).collection("bookmark").document("bookmark");

    Map<String,Object> update=new HashMap<>();

    // 마커 생성
    private Marker marker1 = new Marker();
    private Marker marker2 = new Marker();
    private Marker marker3 = new Marker();

    // DB bookmark 밑에 즐겨찾기 추가한 장소 저장
    private void writeLocation(String location){
        documentReference.update(location,location);
    }

    // 마커 세팅
    private void setMarker(Marker marker, double lat, double lng,String text){

        // DB 북마크에 저장되어 있을 경우 마커 이미지 변경
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot=task.getResult();
                    if(documentSnapshot.contains(text)){
                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_stars_24));
                    }
                    else{
                        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_place_24));
                    }
                }
            }
        });

        marker.setTag(text);
        marker.setWidth(Marker.SIZE_AUTO);
        marker.setHeight(Marker.SIZE_AUTO);
        marker.setPosition(new LatLng(lat,lng));
        marker.setMap(naverMap);
        marker.setOnClickListener(this);
        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_place_24));

        // 마커 클릭 이벤트
        marker.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {

                sub=findViewById(R.id.sub);
                star=findViewById(R.id.bookmark);

                OverlayImage overlayImage = marker.getIcon();
                String overlayString =overlayImage.toString();
                String stringPlace="OverlayImage{id='resource:7f08007b'}";
                String stringStar="OverlayImage{id='resource:7f08007e'}";

                // 북마크 별 이미지
                if(overlayString.equals(stringPlace)){
                    star.setSelected(false);
                }
                else if(overlayString.equals(stringStar)){
                    star.setSelected(true);
                }

                location_name=findViewById(R.id.location_name);
                location_name.setText(text);
                sub.setVisibility(View.VISIBLE);

                star.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // 북마크 취소
                        if(star.isSelected()){
                            star.setSelected(false);
                            marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_place_24));
                            update.put(text,FieldValue.delete());
                            documentReference.update(update);
                        }
                        // 북마크 저장
                        else{
                            star.setSelected(true);
                            marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_stars_24));
                            writeLocation(text);
                        }
                    }
                });
                return true;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //네이버 지도
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Intent intent = new Intent(MainActivity.this, BookmarkActivity.class);
        intent.putExtra("key",id);

        //즐겨찾기 목록 이동 버튼
        Button page_button = findViewById(R.id.bookmark_page);
        page_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });
        update.put("default","default");
        documentReference.set(update,SetOptions.merge());
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap)
    {
        this.naverMap = naverMap;
        naverMap.setOnMapClickListener(this);

        //배경 지도 선택
        naverMap.setMapType(NaverMap.MapType.Basic);

        //건물 표시
        naverMap.setLayerGroupEnabled(naverMap.LAYER_GROUP_BUILDING, true);

        //위치 및 각도 조정
        CameraPosition cameraPosition = new CameraPosition(
                new LatLng(37.62775598632535, 127.09060868681021),  // 위치 지정
                15,
                45,
                -44// 줌 레벨
        );
        naverMap.setCameraPosition(cameraPosition);
        // 마커 표시
        insert_marker(0);
    }

    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        return false;
    }

    // 마커 정보
    private void insert_marker(int a) {
        if(a==0){
            setMarker(marker1,37.62930746865567,127.09034686363627,"제2과학관");
            setMarker(marker2,37.628012365545146,127.09232350005925,"인문사회관");
            setMarker(marker3,37.62908389920287,127.08962192455414,"제1과학관");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
        sub=findViewById(R.id.sub);
        sub.setVisibility(View.INVISIBLE);

        //위도 경도 출력
        //Toast.makeText(getApplicationContext(),"좌표 "+latLng,Toast.LENGTH_LONG).show();

    }
}