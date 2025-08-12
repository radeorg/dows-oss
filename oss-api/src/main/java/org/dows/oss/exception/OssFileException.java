package org.dows.oss.exception;

import org.dows.rade.exception.RadeException;
import org.dows.rade.status.CommonStatusCode;
import org.dows.rade.status.StatusCode;

public class OssFileException extends RadeException {

    public OssFileException() {
    }

    public OssFileException(String msg) {
        super(Integer.valueOf(CommonStatusCode.FAILED.getCode()), msg);
    }

    public OssFileException(Integer code, String msg) {
        super(msg);
    }

    public OssFileException(Integer code, String msg, Throwable e) {
        super(msg, e);
    }

    public OssFileException(Object data) {
        super(data);
    }

    public OssFileException(Throwable throwable) {
        super(throwable);
    }

    public OssFileException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public OssFileException(StatusCode statusCode) {
        super(statusCode.getDescribe());
        this.statusCode = statusCode;
    }

    public OssFileException(StatusCode statusCode, Exception exception) {
        super(String.format(statusCode.getDescribe(), exception.getMessage()));
        this.statusCode = statusCode;
    }

    public OssFileException(StatusCode statusCode, String msg) {
        super(String.format(statusCode.getDescribe(), msg));
        this.statusCode = statusCode;
    }

    public OssFileException(StatusCode statusCode, Object[] args, String message) {
        super(message);
        this.statusCode = statusCode;
        this.args = args;
    }

    public OssFileException(StatusCode statusCode, Object[] args, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.args = args;
    }
}
