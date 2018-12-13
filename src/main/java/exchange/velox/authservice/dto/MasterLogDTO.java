package exchange.velox.authservice.dto;

import java.io.Serializable;

public class MasterLogDTO implements Serializable {

    private static final long serialVersionUID = 6372597147702493642L;

    private String authorID;
    private String authorFullName;
    private String authorEmail;
    private Integer authorSearchableId;
    private String role;
    private VeloxEventType veloxEventType;
    private String logData;

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getAuthorFullName() {
        return authorFullName;
    }

    public void setAuthorFullName(String authorFullName) {
        this.authorFullName = authorFullName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public Integer getAuthorSearchableId() {
        return authorSearchableId;
    }

    public void setAuthorSearchableId(Integer authorSearchableId) {
        this.authorSearchableId = authorSearchableId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public VeloxEventType getVeloxEventType() {
        return veloxEventType;
    }

    public void setVeloxEventType(VeloxEventType veloxEventType) {
        this.veloxEventType = veloxEventType;
    }

    public String getLogData() {
        return logData;
    }

    public void setLogData(String logData) {
        this.logData = logData;
    }

    private MasterLogDTO() {
    }

    private MasterLogDTO(VeloxEventType veloxEventType) {
        this.veloxEventType = veloxEventType;
    }

    public static class MasterLogBuilder {

        private MasterLogDTO masterLogDTO;

        public MasterLogBuilder(VeloxEventType veloxEventType) {
            masterLogDTO = new MasterLogDTO(veloxEventType);
        }

        public MasterLogBuilder withAuthor(UserDTO auth) {
            if (auth == null) {
                return this;
            }
            masterLogDTO.setAuthorEmail(auth.getEmail());
            masterLogDTO.setAuthorFullName(auth.getFullName());
            masterLogDTO.setAuthorID(auth.getId());
            // masterLogDTO.setAuthorSearchableId(auth.getHumanId());
            masterLogDTO.setRole(auth.getRole());
            return this;
        }

        public MasterLogBuilder logData(String logData) {
            masterLogDTO.setLogData(logData);
            return this;
        }

        public MasterLogDTO build() {
            return this.masterLogDTO;
        }
    }

    @Override
    public String toString() {
        return "MasterLogDTO{" +
                    "authorID='" + authorID + '\'' +
                    ", authorFullName='" + authorFullName + '\'' +
                    ", authorEmail='" + authorEmail + '\'' +
                    ", authorSearchableId=" + authorSearchableId +
                    ", role='" + role + '\'' +
                    ", veloxEventType=" + veloxEventType +
                    ", logData='" + logData + '\'' +
                    '}';
    }
}
