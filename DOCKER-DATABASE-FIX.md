# 🔧 Docker Database Connection Fix

## 🚨 **Problem Solved:**

**Error**: `Cannot resolve reference to bean 'jpaSharedEM_entityManagerFactory'`  
**Root Cause**: Docker container trying to connect to `localhost:5432` (refers to container, not host)  
**Solution**: Use `host.docker.internal` for Docker Desktop or proper container networking  

---

## ✅ **What Was Fixed:**

### **1. Created Docker-Specific Profile**
- **File**: `application-docker.yml`
- **Database URL**: `jdbc:postgresql://host.docker.internal:5432/burnbox`
- **Profile**: `docker` (activated automatically in container)

### **2. Updated Dockerfile**
- **Spring Profile**: Automatically set to `docker`
- **Environment Variables**: Support for all database settings
- **Network**: Compatible with Docker Desktop networking

### **3. Added Test Scripts**
- **`test-docker.sh`**: Test with existing PostgreSQL
- **`test-docker-compose.sh`**: Complete stack with PostgreSQL included
- **`docker-compose.test.yml`**: Full testing environment

---

## 🚀 **Testing Options:**

### **Option 1: Use Existing PostgreSQL (Recommended)**
```bash
# Start PostgreSQL locally first
brew services start postgresql

# Test the Docker container
./test-docker.sh
```

### **Option 2: Complete Docker Stack**
```bash
# Includes PostgreSQL in Docker
./test-docker-compose.sh
```

### **Option 3: Manual Docker Run**
```bash
# Build image
docker build -t burnbox-backend .

# Run with environment variables
docker run --rm \
    --name burnbox-test \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=docker \
    -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/burnbox \
    -e DB_USERNAME=burnbox_user \
    -e DB_PASSWORD=burnbox_password \
    burnbox-backend
```

---

## 🔍 **Network Configuration Explained:**

### **Development (Local)**
```yaml
url: jdbc:postgresql://localhost:5432/burnbox
```

### **Docker Desktop**
```yaml
url: jdbc:postgresql://host.docker.internal:5432/burnbox
```

### **Docker Compose**
```yaml
url: jdbc:postgresql://postgres-test:5432/burnbox
```

### **Production (Render)**
```yaml
url: ${DATABASE_URL}  # Provided by Render
```

---

## 📋 **Environment Variables:**

| Variable | Docker | Render | Description |
|----------|--------|--------|-------------|
| `SPRING_PROFILES_ACTIVE` | `docker` | `render` | Configuration profile |
| `DATABASE_URL` | `jdbc:postgresql://host.docker.internal:5432/burnbox` | Auto-provided | Database connection |
| `DB_USERNAME` | `burnbox_user` | Auto-provided | Database user |
| `DB_PASSWORD` | `burnbox_password` | Auto-provided | Database password |
| `PORT` | `8080` | Auto-provided | Application port |

---

## 🎯 **Health Check Endpoints:**

After starting the container, test these endpoints:

| Endpoint | Purpose | Expected Response |
|----------|---------|-------------------|
| `http://localhost:8080/health` | Custom health | `{"status":"UP"}` |
| `http://localhost:8080/actuator/health` | Spring health | Detailed health info |
| `http://localhost:8080/health/db` | Database health | `{"status":"UP","database":"PostgreSQL"}` |

---

## 🐛 **Troubleshooting:**

### **PostgreSQL Connection Issues:**
```bash
# Check if PostgreSQL is running
pg_isready -h localhost -p 5432

# Start PostgreSQL
brew services start postgresql

# Check Docker network
docker network ls
```

### **Container Logs:**
```bash
# View application logs
docker logs burnbox-test

# View docker-compose logs
docker-compose -f docker-compose.test.yml logs burnbox-app
```

### **Database Debugging:**
```bash
# Connect to PostgreSQL directly
psql -h localhost -U burnbox_user -d burnbox

# Check database exists
\l

# Check tables
\dt
```

---

## 🎉 **Success Criteria:**

✅ **Container starts without errors**  
✅ **Health endpoints return 200 OK**  
✅ **Database connection established**  
✅ **JPA entities loaded successfully**  
✅ **Application ready to accept requests**  

---

## 📦 **Files Added/Modified:**

- ✅ `application-docker.yml` - Docker-specific configuration
- ✅ `Dockerfile` - Updated with Docker profile
- ✅ `test-docker.sh` - Test script for existing PostgreSQL
- ✅ `test-docker-compose.sh` - Complete stack test
- ✅ `docker-compose.test.yml` - Testing environment

---

**The Docker database connection issue is now resolved!** 🎉