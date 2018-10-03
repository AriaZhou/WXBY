package com.example.joan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.example.joan.myapplication.database.model.BaseModel;
import com.example.joan.myapplication.database.model.LegalCounselingModel;
import com.example.joan.myapplication.database.repository.CounselingRepositoryImpl;

import net.sf.json.JSONArray;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

public class MyConsultListActivity extends AppCompatActivity implements MyLawyerConsultLayout.OnRootClickListener{

    private SharedPreferences sp;
    List<LegalCounselingModel > counselingList = new ArrayList<>();
    LinearLayout lawyer_consult_list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_consult_list);
        lawyer_consult_list = findViewById(R.id.main_body);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        initLawyerView();

    }

    private void initLawyerView(){
        sp = getSharedPreferences("account_info", Context.MODE_PRIVATE);
        try{
            RequestParams params = new RequestParams("http://" + BaseModel.IP_ADDR +":8080/searchCounseling.action");
            params.addQueryStringParameter("condition",sp.getString("_id","0"));//當前使用者id
//            params.addQueryStringParameter("condition","222");//當前使用者id
            params.addQueryStringParameter("type","3");
            x.http().get(params, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    JSONArray jArray= JSONArray.fromObject(s);
                    counselingList = new CounselingRepositoryImpl().convert(jArray);
                    System.out.println(counselingList.get(0).getContent().get(0).getQuestion());

                    CounselingView();
                }

                @Override
                public void onError(Throwable throwable, boolean b) {

                }

                @Override
                public void onCancelled(CancelledException e) {

                }

                @Override
                public void onFinished() {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void CounselingView(){
        if(counselingList.isEmpty()){
            lawyer_consult_list.addView(new FindNothingView(this).init());
        }else{
            int index = 0;
            for (LegalCounselingModel counseling: counselingList
                    ) {
                String question = counseling.getContent().get(0).getQuestion();

                String createTime = counseling.getCreateTime().replace('T',' ');
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String dateString = formatter.format(createTime);

                String name = counseling.getQuestioner();
                String job = "";
                int state = 0;

                lawyer_consult_list.addView(new MyLawyerConsultLayout(this)
                        .init(name,job,state,question, createTime)
                        .setOnRootClickListener(this, index));
                index++;
            }
        }
    }

    @Override
    public void onRootClick(View v) {
        LegalCounselingModel counseling = counselingList.get((int)v.getTag());
        Intent intent = new Intent(MyConsultListActivity.this, MyAnswerLawyerConsultActivity.class);
        intent.putExtra("counseling", counseling);
        startActivity(intent);
    }

}