// 북마크 목록 액티비티
package com.test.navermap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookmarkActivity extends AppCompatActivity {

    //회원 아이디 값
    String id;

    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private DocumentReference documentReference;

    Map<String,Object> update=new HashMap<>();

    ArrayList<BookmarkList> dataList;
    String Text;
    String f;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark);

        Intent intent=getIntent();
        id=intent.getStringExtra("key");

        documentReference=firebaseFirestore.collection("users").document(id).collection("bookmark").document("bookmark");

        this.InitializeData();

        ListView listView=findViewById(R.id.listView);
        final BookmarkAdapter bookmarkAdapter=new BookmarkAdapter(this, dataList);

        listView.setAdapter(bookmarkAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),dataList.get(position).getList_name(),Toast.LENGTH_LONG).show();
                dataList.remove(position);
                bookmarkAdapter.notifyDataSetInvalidated();
            }
        });

        // 뒤로가기 버튼
        ImageView back_button=findViewById(R.id.back);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookmarkActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });


        // 북마크 목록 가져오기
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String j=documentSnapshot.getData().toString();
                    String[] jArray=j.split(",|=");
                    for(int i=2; i< jArray.length; i+=2){
                        Text=jArray[i].trim();
                        dataList.add(new BookmarkList(Text));
                        bookmarkAdapter.notifyDataSetInvalidated();
                    }
                }
            }
        });
    }

    public void InitializeData() {
        dataList = new ArrayList<BookmarkList>();
    }

    public class BookmarkAdapter extends BaseAdapter{

        Context mContext;
        ArrayList<BookmarkList> bookmarkList;
        LayoutInflater mLayoutInflater;

        public BookmarkAdapter(Context context, ArrayList<BookmarkList> list){
            mContext=context;
            bookmarkList=list;
            mLayoutInflater=LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return bookmarkList.size();
        }

        @Override
        public BookmarkList getItem(int position) {
            return bookmarkList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View view=mLayoutInflater.inflate(R.layout.view_text,null);
            TextView list_name=view.findViewById(R.id.list_name);
            ImageView list_image=view.findViewById(R.id.list_image);
            list_name.setText(bookmarkList.get(position).getList_name());
            FrameLayout list_full=view.findViewById(R.id.list_full);
            String r=bookmarkList.get(position).getList_name();

            Button button1=findViewById(R.id.button1);
            Button button2=findViewById(R.id.button2);

            // 편집 버튼
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    button1.setVisibility(View.INVISIBLE);
                    button2.setVisibility(View.VISIBLE);
                }
            });

            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    button2.setVisibility(View.INVISIBLE);
                    button1.setVisibility(View.VISIBLE);
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            });

            // 북마크 목록 클릭 (편집)
            list_full.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                    builder.setTitle("");
                    builder.setMessage("삭제하시겠습니까?");
                    builder.setIcon(R.drawable.ic_baseline_warning_24);

                    // Yes 버튼 및 이벤트 생성
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            list_image.setImageResource(R.drawable.ic_baseline_remove_circle_outline_24);
                            update.put(r,FieldValue.delete());
                            documentReference.update(update);
                        }
                    });
                    //Cancel 버튼 및 이벤트 생성
                    builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    //No 버튼 및 이벤트 생성
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Pass
                            list_image.setImageResource(R.drawable.ic_baseline_stars_24);
                            documentReference.update(r,r);
                        }
                    });

                    if(button2.getVisibility()==View.VISIBLE){
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
            f=bookmarkList.get(position).toString();
            return view;
        }
    }

}
