package exchange.velox.authservice.dto;

public enum UserRole {
    ADMIN("Admin"),
    CREDIT_ANALYST("Credit Analyst"),
    DATA_ENTRY("Data Entry"),
    BIDDER_COMPANY("Investor Company"),
    BIDDER("Investor"),
    SELLER_COMPANY("Seller Company"),
    SELLER("Seller"),
    INTRODUCER("Introducer");

    private String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
