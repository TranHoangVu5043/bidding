package Client.model.user;

public class Seller  extends User {
    private String citizendId;
    private String bankAccount;

    public Seller(String citizendId, String bankAccount) {
        super();
        this.citizendId = citizendId;
        this.bankAccount = bankAccount;
    }

    public String getCitizendId() {
        return citizendId;
    }

    public void setCitizendId(String citizendId) {
        this.citizendId = citizendId;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
}
