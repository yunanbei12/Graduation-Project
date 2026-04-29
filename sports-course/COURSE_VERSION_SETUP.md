# sports-course Setup

This directory is the course-only graduation-project edition cloned from `sports`.

## Reserved Full Version

- Full project code: `/Users/atrox/集美大学/Graduation-Project/sports`
- Full project database: `kinetic_sports`

## Course Version Defaults

- Course project code: `/Users/atrox/集美大学/Graduation-Project/sports-course`
- Course project database: `kinetic_sports_course`
- Admin backend port: `8185`
- Mini-app API port: `8186`

## Empty Database

Create an empty course database:

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS kinetic_sports_course DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
```

Then initialize only the course-version schema:

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 kinetic_sports_course < /Users/atrox/集美大学/Graduation-Project/sports-course/shop-back-end/db/kinetic_sports_course.sql
```

If the database was initialized before the latest course-version schema updates, run this upgrade script once:

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 kinetic_sports_course < /Users/atrox/集美大学/Graduation-Project/sports-course/shop-back-end/db/migration_course_schema_fix.sql
```

Install admin frontend dependencies before starting Vite:

```bash
cd /Users/atrox/集美大学/Graduation-Project/sports-course/sports-admin-web
npm ci
```

After the schema is initialized, all mall-related cleanup should be performed only in this `sports-course` project and against `kinetic_sports_course`.
