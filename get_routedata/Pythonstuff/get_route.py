import requests


def is_downloadable(url):
    h = requests.head(url, allow_redirects=True)
    header = h.headers
    content_type = header.get('content-type')
    if 'text' in content_type.lower():
        return False
    if 'html' in content_type.lower():
        return False
    return True

# <--Todo | Input --> Room --> Video<name>

# videos.json: https://services.guide3d.com/menu/cors/index.php?project=100011&language=de&set=set_01&force-display=false

# GET: https://cdnguide3dcom.blob.core.windows.net/videos/100011/544x306/L00P1133-<point>-M0000.mp4
# request Room ---Donwload-> Video
# L00P1133-'point'-M0000.mp4
filename = 'L00P1133-L01P1056-M0000.mp4'
is_downloadable('https://cdnguide3dcom.blob.core.windows.net/videos/100011/544x306/L00P1133-L01P1056-M0000.mp4')

#======================================================================================================================#
#                            Get videos.json from cdnguide3dcom.blob.core.windows.net                                  #
#======================================================================================================================#

rq = requests.get(
    'https://services.guide3d.com/menu/cors/index.php?project=100011&language=de&set=set_01&force-display=false')
videos = rq.json()

#======================================================================================================================#
#                       Get-request for downloading videos and generate the correct name                               #
#======================================================================================================================#

print(range(len(videos['data']['list'])))


for video in range(len(videos['data']['list'])):
    point = videos['data']['list'][video]['point']
    room = videos['data']['list'][video]['name']

    # url = f'https://services.guide3d.com/route/cors/index.php?project=100011&start=L00P1133&end={point}&mode=M0000&redirect=duration&format=none'

    url = f'https://cdnguide3dcom.blob.core.windows.net/videos/100011/544x306/L00P1133-{point}-M0000.mp4'

    print(f'Download point: {point}, room:{room}')
    print(f'Link: {url}')
    r = requests.get(url, allow_redirects=True)

    print(f'rename L00P1133-{point}-M0000.mp4 --> room:{room}.mp4')
    open(f'{room}.mp4', 'wb').write(r.content)
