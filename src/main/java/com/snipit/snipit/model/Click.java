package com.snipit.snipit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "clicks")
public class Click {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "url_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Url url;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    @Column(length = 50)
    private String browser;

    @Column(length = 20)
    private String deviceType;

    @Column(length = 255)
    private String referrer;

    public Click() {
    }

    @PrePersist
    protected void onCreate() {
        this.clickedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Url getUrl() { return url; }
    public void setUrl(Url url) { this.url = url; }

    public LocalDateTime getClickedAt() { return clickedAt; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }
}