@echo off
chcp 65001 > nul
echo ========================================================
echo        منصة Dadix Web AI - تشغيل بيئة العمل
echo ========================================================
echo.
echo [1/3] فحص توفر بيئة Java و Android SDK...
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [تنبيه] لم يتم العثور على Java مثبتة في النظام. يرجى تثبيت JDK 17 أو JDK 21.
    pause
    exit /b 1
)

echo [2/3] بناء وتشغيل تطبيق Dadix Web AI...
gradle assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo [3/3] تم بناء حزمة التطبيق بنجاح في مجلد:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo يمكنك تشغيلها مباشرة على محاكي Android أو نقل ملف الـ APK إلى هاتفك!
) else (
    echo.
    echo [خطأ] حدث خطأ أثناء تجميع المشروع. يرجى التحقق من سجل الأخطاء أعلاه.
)

pause
