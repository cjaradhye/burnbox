package com.disposablemailservice.model;

public class MailboxRequest {
    private int lifespan;
    private boolean burnAfterRead;
    private String emailName;

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    public boolean isBurnAfterRead() {
        return burnAfterRead;
    }

    public void setBurnAfterRead(boolean burnAfterRead) {
        this.burnAfterRead = burnAfterRead;
    }

    public String getEmailName() {
        return emailName;
    }

    public void setEmailName(String emailName) {
        this.emailName = emailName;
    }

    @Override
    public String toString() {
        return "MailboxRequest{" +
                "lifespan=" + lifespan +
                ", burnAfterRead=" + burnAfterRead +
                ", emailName='" + emailName + '\'' +
                '}';
    }
}
