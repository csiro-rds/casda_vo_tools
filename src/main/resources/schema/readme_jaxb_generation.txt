Generating jaxb classes using xjc

Found some info here:
http://blog.bdoughan.com/2011/12/reusing-generated-jaxb-classes.html
https://weblogs.java.net/blog/kohsuke/archive/2006/09/separate_compil.html

from c:/temp with local xsd files and destination folder created: 

C:\temp>xjc -episode voresource.episode -d C:\temp\voresource -p au.csiro.casda.votools.jaxb.voresource VOResource.xsd

C:\temp>xjc -episode capabilities.episode -d C:\temp\capabilities -p au.csiro.casda.votools.jaxb.capabilities capabilities.xsd -b voresource.episode -extension

C:\temp>xjc -episode availability.episode -d C:\temp\availability -p au.csiro.casda.votools.jaxb.availability availability.xsd

--This generated an empty episode file and just an object factory - so I did not check it in.
C:\temp>xjc -episode xlink.episode -d C:\temp\xlink -p au.csiro.casda.votools.jaxb.xlink xlink.xsd

C:\temp>xjc -episode stc.episode -d C:\temp\stc -p au.csiro.casda.votools.jaxb.stc stc.xsd

C:\temp>xjc -episode vodataservice.episode -d C:\temp\vodataservice -p au.csiro.casda.votools.jaxb.vodataservice vodataservice.xsd -b stc.episode -b voresource.episode -extension

--XSD was broken had to add to namespaces (xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.1")
C:\temp>xjc -episode vositables.episode -d C:\temp\vositables -p au.csiro.casda.votools.jaxb.vositables vositables.xsd -b vodataservice.episode -b voresource.episode -b stc.episode -extension

C:\temp>xjc -episode tapregext.episode -d C:\temp\tapregext -p au.csiro.casda.votools.jaxb.tapregext tapregext.xsd -b voresource.episode -extension
