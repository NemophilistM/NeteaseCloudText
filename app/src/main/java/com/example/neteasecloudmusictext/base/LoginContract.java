package com.example.neteasecloudmusictext.base;

public interface LoginContract {

    public interface ILoginView {

        public void onSuccess(String userName);
    }
    public interface ILoginPresenter<ILoginView> {

        public void detachView();
        public void requestLoginData(String userName, String password);
    }


    public interface ILoginModel {
        public void containLoginResponseData(String userName, String password, CallBack callBack);

        public interface CallBack {

            public void onSuccess();

        }
    }
}
