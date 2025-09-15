# 🚀 Burnbox - Render Deployment Guide

## 📋 **Quick Deploy Checklist**

✅ **Repository Ready**: All Render configurations added to `prod` branch  
✅ **Infrastructure as Code**: `render.yaml` defines all services  
✅ **Docker Optimized**: Multi-stage build with health checks  
✅ **Environment Config**: Spring profile for Render platform  
✅ **Health Monitoring**: Custom health endpoints + Spring Actuator  

---

## 🎯 **One-Click Deploy to Render**

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/cjaradhye/burnbox-backend&branch=prod)

---

## 🛠️ **Manual Setup Steps**

### **Step 1: Connect Repository**
1. Go to [render.com](https://render.com) and sign up
2. Click **"New +"** → **"Blueprint"**
3. Connect GitHub account
4. Select `burnbox-backend` repository
5. Choose `prod` branch
6. Click **"Apply"**

### **Step 2: Configure Environment Variables**
Add these in Render Dashboard:

```bash
# 🔑 Authentication
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here

# 🔐 JWT Security
JWT_SECRET=your-super-long-256-bit-secret-make-it-unique-and-secure-for-production
JWT_EXPIRATION=86400000

# 🌐 Application URLs
EMAIL_DOMAIN=nahneedpfft.com
FRONTEND_URL=https://your-frontend-app.onrender.com
CORS_ALLOWED_ORIGINS=https://your-frontend-app.onrender.com,http://localhost:3000

# ☁️ AWS (Optional)
AWS_REGION=us-west-2
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_S3_BUCKET=burnbox-email-storage
SES_FROM_EMAIL=noreply@nahneedpfft.com
```

### **Step 3: Services Created**
The `render.yaml` blueprint creates:

| Service | Type | Plan | Purpose |
|---------|------|------|---------|
| `burnbox-backend` | Web Service | Starter | Main API |
| `burnbox-postgres` | PostgreSQL | Starter | Database |
| `burnbox-redis` | Redis | Starter | Cache |

---

## 🏗️ **Architecture Overview**

```
┌─────────────────┐    ┌──────────────┐    ┌─────────────┐
│   Frontend      │────│   Burnbox    │────│ PostgreSQL  │
│   (React)       │    │   Backend    │    │ Database    │
└─────────────────┘    │   (Docker)   │    └─────────────┘
                       └──────────────┘           │
                              │                   │
                       ┌──────────────┐    ┌─────────────┐
                       │    Redis     │    │    AWS      │
                       │    Cache     │    │  Services   │
                       └──────────────┘    └─────────────┘
```

---

## 🔍 **Health Check Endpoints**

| Endpoint | Purpose | Response |
|----------|---------|----------|
| `/health` | Basic health check | `{"status":"UP"}` |
| `/health/db` | Database connectivity | `{"status":"UP","database":"PostgreSQL"}` |
| `/health/ready` | Readiness probe | `{"status":"READY"}` |
| `/health/live` | Liveness probe | `{"status":"ALIVE"}` |
| `/actuator/health` | Spring Boot health | Detailed health info |

---

## 🎨 **Custom Domain Setup**

### **Step 1: Add Domain in Render**
1. Go to your service → **Settings** → **Custom Domains**
2. Add your domain: `api.yourdomain.com`

### **Step 2: Configure DNS**
Add this CNAME record to your DNS:
```
CNAME   api.yourdomain.com   → your-app-name.onrender.com
```

### **Step 3: Update Environment**
```bash
FRONTEND_URL=https://yourdomain.com
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://api.yourdomain.com
```

---

## 🔐 **Security Configuration**

### **Google OAuth2 Setup**
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. **APIs & Services** → **Credentials**
3. Edit OAuth 2.0 Client ID
4. Add authorized redirect URI:
   ```
   https://your-app-name.onrender.com/login/oauth2/code/google
   ```

### **JWT Secret Generation**
```bash
# Generate secure 256-bit secret
openssl rand -base64 32
```

---

## 📊 **Monitoring & Logs**

### **Render Built-in Monitoring**
- **Service Logs**: Real-time application logs
- **Metrics**: CPU, Memory, Network usage
- **Health Checks**: Automatic endpoint monitoring
- **Alerts**: Email notifications for failures

### **Application Metrics**
- **Spring Actuator**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`
- **Health Details**: `/actuator/health`

---

## 💰 **Render Pricing**

### **Free Tier (Great for Testing)**
- **Web Services**: 750 hours/month
- **PostgreSQL**: 1GB storage
- **Redis**: 25MB storage
- **Auto-sleep**: After 15 minutes inactivity

### **Starter Tier ($7/month per service)**
- **Always On**: No auto-sleep
- **Custom Domains**: Free SSL certificates
- **More Resources**: Better performance
- **Priority Support**: Faster build times

---

## 🚨 **Troubleshooting**

### **Build Failures**
```bash
# Check Dockerfile syntax
docker build -t burnbox-test .

# Verify Java version
java -version  # Should be 17+
```

### **Database Connection Issues**
```bash
# Check environment variables
echo $DATABASE_URL

# Test database connectivity
curl https://your-app.onrender.com/health/db
```

### **OAuth2 Redirect Issues**
- Verify Google OAuth2 redirect URIs match exactly
- Check `RENDER_EXTERNAL_URL` environment variable
- Ensure HTTPS (not HTTP) in redirect URIs

### **Performance Issues**
- Upgrade to Starter plan (no auto-sleep)
- Check resource usage in Render dashboard
- Monitor logs for memory/CPU issues

---

## 🎯 **Deployment URLs**

After deployment, your API will be available at:

```
🌐 Main API: https://your-app-name.onrender.com
🏥 Health: https://your-app-name.onrender.com/health
📊 Metrics: https://your-app-name.onrender.com/actuator/health
🔐 OAuth2: https://your-app-name.onrender.com/oauth2/authorization/google
📧 Mailboxes: https://your-app-name.onrender.com/api/mailboxes
```

---

## ✅ **Post-Deployment Checklist**

- [ ] Health endpoints responding (200 OK)
- [ ] Database connection working
- [ ] Redis cache connected
- [ ] OAuth2 login flow working
- [ ] API endpoints responding
- [ ] CORS configured for frontend
- [ ] Environment variables set
- [ ] Custom domain configured (optional)
- [ ] Monitoring alerts set up

---

## 🎉 **Success!**

Your Burnbox backend is now live on Render! 

**Next Steps:**
1. Deploy your frontend to Render/Vercel/Netlify
2. Set up AWS SES for email handling
3. Configure custom domain
4. Set up monitoring alerts

**Support:** Check Render documentation or contact support for issues.

---

*Generated for Burnbox Backend v1.0.0 - Render Deployment*