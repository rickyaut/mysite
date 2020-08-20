from datetime import datetime
from lxml import etree, html
from pytz import timezone
from pathlib import Path
import os

def adjustTimeZone(time):
    melbourneZone = timezone('Australia/Melbourne')
    localizedTime = melbourneZone.localize(datetime.strptime(time[6:25], '%Y-%m-%dT%H:%M:%S'))
    if(localizedTime.strftime('%z') == '+1100'):
        return time[0:29] +"+11:00"
    else:
        return time

for file in Path('pages').rglob('*.xml'):
    tree = etree.parse(os.path.abspath(file))
    eventDetail = tree.xpath("//event")
    if(len(eventDetail) > 0):
        startDateTime = adjustTimeZone(eventDetail[0].get('startDateTime'))
        endDateTime = adjustTimeZone(eventDetail[0].get('endDateTime'))
        eventDetail[0].attrib['startDateTime'] = startDateTime
        eventDetail[0].attrib['endDateTime'] = endDateTime
        prefix_map = {"sling": "http://sling.apache.org/jcr/sling/1.0"}
        for element in tree.xpath(".//*[@sling:resourceType='mysite/components/event']", namespaces=prefix_map):
            try:
                textTree = html.fromstring(element.get('text'))
                for anchor in textTree.xpath("//a"):
                    href = anchor.get("href")
                    target = anchor.get("target")
                    if target and href and (href.startswith("http://mysite") or href.startswith("https://mysite")):
                        anchor.attrib.pop("target")
                element.attrib['text']= html.tostring(textTree)
            except:
                pass
        if(len(tree.xpath(".//speakers_accordion"))>0):
            speakerAccordion = tree.xpath(".//speakers_accordion")[0]
            if(len(speakerAccordion.xpath(".//text")) == 0):
                speakerAccordion.xpath("..")[0].remove(speakerAccordion)
            with open(file, 'wb') as f:
                f.write(etree.tostring(tree))

os.remove('afile.xml')
