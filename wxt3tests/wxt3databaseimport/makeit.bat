REM setting path to this batfiles catalog
cd /d %~dp0
mysql -e "select dice,name from wines where country='Spania' and dice=6" > batselect.xml --default-character-set=utf8 -X --user=student --password=student --host=frigg.hiof.no vin
