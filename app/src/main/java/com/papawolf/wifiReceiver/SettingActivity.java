package com.papawolf.wifiReceiver;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SettingActivity extends Activity {

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    static final String[] LIST_MENU = {"Reverse", "EPA", "Trim"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_setting);

        ListView listview ;
        CustomChoiceListViewAdapter adapter;

        // Adapter 생성
        adapter = new CustomChoiceListViewAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.listViewReverse);
        listview.setAdapter(adapter);

        // 첫 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_gamepad_black_18dp),
                "Channel 1") ;
        // 두 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_gamepad_black_18dp),
                "Channel 2") ;
        // 세 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_gamepad_black_18dp),
                "Channel 3") ;
        // 세 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_gamepad_black_18dp),
                "Channel 4") ;

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Dlog.d("Item Click" + position + " " + id + " ");

                CheckableLinearLayout cb = (CheckableLinearLayout) v;
                Dlog.d("cb cb" + cb.isChecked());
            }
        });


//        expListView = (ExpandableListView) findViewById(R.id.expListViewSetting);
//
//        // 리스트뷰에 데이터를 넣는 곳.
//        ChildListData();
//
//        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
//
//        // 리스트어댑터 세팅
//        expListView.setAdapter(listAdapter);
//
//        // 리스트뷰 그룹(부모)뷰를 클릭 했을 경우
//        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//
//            @Override
//            public boolean onGroupClick(ExpandableListView parent, View v,
//                                        int groupPosition, long id) {
//                // Toast.makeText(getApplicationContext(),
//                // "Group Clicked " + listDataHeader.get(groupPosition),
//                // Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
//
//        // 그룹이 열릴 경우 이벤트 발
//        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//
//            @Override
//            public void onGroupExpand(int groupPosition) {
//                Toast.makeText(getApplicationContext(),
//                        listDataHeader.get(groupPosition) + " Expanded",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // 그룹이 닫힐 경우 이벤트 발생
//        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
//
//            @Override
//            public void onGroupCollapse(int groupPosition) {
//                Toast.makeText(getApplicationContext(),
//                        listDataHeader.get(groupPosition) + " Collapsed",
//                        Toast.LENGTH_SHORT).show();
//
//            }
//        });
//
//        // 차일드 뷰를 눌렀을 경우 이벤트 발생
//        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v,
//                                        int groupPosition, int childPosition, long id) {
//                // TODO Auto-generated method stub
//                Toast.makeText(
//                        getApplicationContext(),
//                        listDataHeader.get(groupPosition)
//                                + " : "
//                                + listDataChild.get(
//                                listDataHeader.get(groupPosition)).get(
//                                childPosition), Toast.LENGTH_SHORT)
//                        .show();
//                return false;
//            }
//        });
    }

    /**
     * 부모 뷰 타이틀 및 차일드 뷰 데이터 넣는 곳
     */
    private void ChildListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // 그룹 생성
        listDataHeader.add("Reverse");
        listDataHeader.add("End Point Adjust");
        listDataHeader.add("Trim");

        // 그룹 내 차일드 뷰 생성
        List<String> reverse = new ArrayList<String>();
        reverse.add("Channel 1");
        reverse.add("Channel 2");
        reverse.add("Channel 3");
        reverse.add("Channel 4");


        List<String> epa = new ArrayList<String>();
        epa.add("Channel 1");
        epa.add("Channel 2");
        epa.add("Channel 3");
        epa.add("Channel 4");

        List<String> trim = new ArrayList<String>();
        trim.add("Channel 1");
        trim.add("Channel 2");
        trim.add("Channel 3");
        trim.add("Channel 4");

        //데이터 적용.
        listDataChild.put(listDataHeader.get(0), reverse);
        listDataChild.put(listDataHeader.get(1), epa);
        listDataChild.put(listDataHeader.get(2), trim);
    }
}
