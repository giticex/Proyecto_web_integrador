# Guía de despliegue

La aplicación está preparada para ejecutarse con Docker y para publicarse en
Railway, Render o Vercel. Nunca subir contraseñas ni `JWT_SECRET` reales al
repositorio: usar las variables de entorno del proveedor.

## 1. Variables de entorno

### Backend

| Variable | Ejemplo | Obligatoria |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://host:3306/proyecto?useSSL=true` | Sí |
| `DB_USERNAME` | `usuario_bd` | Sí |
| `DB_PASSWORD` | `********` | Sí |
| `JWT_SECRET` | cadena aleatoria de 32+ caracteres | Sí |
| `JWT_EXPIRATION_MS` | `3600000` | No |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` | No |
| `APP_CORS_ALLOWED_ORIGIN` | `https://mi-frontend.vercel.app` | Sí |
| `PORT` | lo inyecta el proveedor | No |

### Frontend

| Variable | Ejemplo |
| --- | --- |
| `VITE_API_URL` | `https://mi-backend.up.railway.app/api` |

## 2. Railway (recomendado para backend + MySQL)

1. Crear un proyecto y agregar un servicio **MySQL**.
2. Agregar un servicio desde el repositorio GitHub y seleccionar la carpeta
   `backend` como contexto, con `Dockerfile` como builder.
3. Configurar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y
   `APP_CORS_ALLOWED_ORIGIN` en *Variables*.
4. Ejecutar `database/script_mysql.sql` en la consola MySQL de Railway (o
   montarlo como init script).
5. Copiar el dominio público del backend, por ejemplo
   `https://techzone-api.up.railway.app`.

Para el frontend se puede usar Vercel (sección 4) apuntando
`VITE_API_URL=https://techzone-api.up.railway.app/api`.

## 3. Render

El archivo `render.yaml` de la raíz define:

- `techzone-api`: Web Service con Docker (`backend/Dockerfile`).
- `techzone-web`: Static Site que compila `frontend` y publica `frontend/dist`.

Pasos:

1. Conectar el repositorio en Render y aplicar el Blueprint.
2. Completar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`.
3. En el static site ajustar `VITE_API_URL` con la URL del backend.
4. Agregar el dominio del frontend a `APP_CORS_ALLOWED_ORIGIN` del backend.
5. Re-deploy para aplicar variables.

Render no ofrece MySQL administrado gratuito; usar Aiven, PlanetScale, Railway
MySQL o una instancia externa.

## 4. Vercel (solo frontend)

1. Importar el repositorio.
2. Configurar *Root Directory* = `frontend`.
3. Build command: `npm run build`; Output: `dist`.
4. Variable `VITE_API_URL` = URL pública del backend + `/api`.
5. `frontend/vercel.json` ya incluye el rewrite de SPA.

## 5. Docker en un VPS

```bash
git clone <repo>
cd Proyecto_web_integrador
cp .env.example .env
# editar .env con secretos reales
docker compose up -d --build
```

Publicar los puertos 8081 (frontend) y 8080 (backend) o colocar un reverse
proxy (Caddy/Nginx/Traefik) con HTTPS.

## 6. Checklist de publicación

- [ ] Base de datos inicializada (`database/script_mysql.sql`).
- [ ] Backend responde `POST /api/auth/login` con token.
- [ ] `GET /api/auth/me` funciona con el token.
- [ ] Frontend carga y redirige a `/login`.
- [ ] CORS incluye el dominio exacto del frontend.
- [ ] `JWT_SECRET` distinto al valor de ejemplo.
- [ ] HTTPS activo en frontend y backend.
- [ ] Colección Postman ejecutada contra el entorno publicado.
- [ ] URLs registradas en la portada del README del aula virtual.
