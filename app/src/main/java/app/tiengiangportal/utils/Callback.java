/*
Create by Nguyen Nguyen
 */
package app.tiengiangportal.utils;

public interface Callback<T> {

    void onSuccess(T result);

    void onError(String result);

}
