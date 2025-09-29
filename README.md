Докер:

1) Selenoid
docker stop selenoid && docker rm selenoid

docker run -d \
--name selenoid \
-p 4444:4444 \
-v /var/run/docker.sock:/var/run/docker.sock \
-v /Users/vitaliy/.selenoid/browsers.json:/etc/selenoid/browsers.json \
aerokube/selenoid:latest-release \
-limit 10

2) Selenoid-ui

docker stop selenoid-ui && docker rm selenoid-ui

docker run -d \
--name selenoid-ui \
-p 8090:8080 \
--link selenoid \
aerokube/selenoid-ui --selenoid-uri http://selenoid:4444

/Users/vitaliy/.selenoid/browsers.json
{
"chrome": {
"default": "latest",
"versions": {
"latest": {
"image": "selenoid/chrome:120.0",
"port": "4444",
"path": "/",
"video": true,
"screenResolution": "1920x1080",
"env": ["ENABLE_VNC=true"]
}
}
},
"firefox": {
"default": "latest",
"versions": {
"latest": {
"image": "selenoid/firefox:120.0",
"port": "4444",
"path": "/",
"video": true,
"screenResolution": "1920x1080",
"env": ["ENABLE_VNC=true"]
}
}
}
}



Локальный режим (браузер на Mac):
mvn clean test -DincludeTags=DataFromCsvSource -DbankAppUrl=http://localhost:3000

Selenoid режим (браузер в Docker по ссылке http://localhost:8090/#/):
mvn clean test -DincludeTags=DataFromCsvSource -Dremote.webdriver.url=http://localhost:4444/wd/hub -DbankAppUrl=http://localhost:3000


Запуск в несколько потоков через Selenoid режим (браузер в Docker по ссылке http://localhost:8090/#/):
vitaliy@MacBook-Pro-Vitalii qa-automation-framework % mvn clean test -DincludeTags=DataFromCsvSource -Dremote.webdriver.url=http://localhost:4444/wd/hub -DbankAppUrl=http://localhost:3000 -Dparallel.enabled=true

Запуск в несколько потоков через локальный режим (браузер на Mac):
vitaliy@MacBook-Pro-Vitalii qa-automation-framework % mvn clean test -DincludeTags=DataFromCsvSource -DbankAppUrl=http://localhost:3000 -Dparallel.enabled=true

Формирование алюр отчета:
vitaliy@MacBook-Pro-Vitalii qa-automation-framework % mvn allure:serve


Что нужно настроить:
В pom.xml <parallel.tests>2</parallel.tests>
В junit-platform.properties junit.jupiter.execution.parallel.config.fixed.max-pool-size=2
В junit-platform.properties junit.jupiter.execution.parallel.config.fixed.parallelism=2
