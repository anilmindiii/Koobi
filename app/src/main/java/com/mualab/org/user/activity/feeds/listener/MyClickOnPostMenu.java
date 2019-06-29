package com.mualab.org.user.activity.feeds.listener;

public class MyClickOnPostMenu {

    private static MyClickOnPostMenu mentIntance;
    private GetMenuClick listnerMenu;

    public static MyClickOnPostMenu getMentIntance(){
        if(mentIntance == null){
            mentIntance = new MyClickOnPostMenu();
        }

        return mentIntance;
    }

    public void setMenuClick(){
        if(listnerMenu != null){
            listnerMenu.getMenuClick();
        }
    }

    public interface GetMenuClick{
        void getMenuClick();
    }

    public void setListnerMenu(GetMenuClick listnerMenu){
        this.listnerMenu = listnerMenu;
    }
}
