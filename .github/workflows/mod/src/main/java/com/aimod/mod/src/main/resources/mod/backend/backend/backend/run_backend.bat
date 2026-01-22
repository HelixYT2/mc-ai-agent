@echo off
REM Example backend launcher for Windows dev (assumes Python and requirements installed)
uvicorn main:app --host 127.0.0.1 --port 8000 --reload
pause
