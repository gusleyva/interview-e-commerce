#!/bin/bash
echo "🚀 Starting application in PRODUCTION mode..."
SPRING_PROFILE=prod docker-compose up --build