SET p=C:\wxtproject\wxt4\dist\wxt4.jar
SET testpath=C:\web\wxt3tests
java -jar %p% %testpath%\wxt3content\script.xml
java -jar %p% %testpath%\wxt3databaseimport\script.xml
java -jar %p% %testpath%\wxt3expandable\script.xml
java -jar %p% %testpath%\wxt3footref\script.xml
java -jar %p% %testpath%\wxt3formulas\script.xml
java -jar %p% %testpath%\wxt3fragments\script.xml
java -jar %p% %testpath%\wxt3gadgets\script.xml
java -jar %p% %testpath%\wxt3images\script.xml
java -jar %p% %testpath%\wxt3language\script.xml
java -jar %p% %testpath%\wxt3odtimport\script.xml
java -jar %p% %testpath%\wxt3popfragment\script.xml
java -jar %p% %testpath%\wxt3popup\script.xml
java -jar %p% %testpath%\wxt3textimport\script.xml
java -jar %p% %testpath%\wxt3tocs\script.xml
java -jar %p% %testpath%\wxt3wikiimport\script.xml
java -jar %p% %testpath%\wxt3tidy\script.xml
java -jar %p% %testpath%\wxt3html5\script.xml
pause