package exchange.velox.authservice.dto;

public enum ClientType {
    WEB("Desktop"),
    IOS("Mobile Phone"),
    ANDROID("Mobile Phone");

    private String desc;

    ClientType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
