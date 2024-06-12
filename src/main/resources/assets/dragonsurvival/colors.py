import os
import json
from PIL import Image

filepath = "./textures/dragon/custom"
hueavg = {}
for dp in os.walk(filepath):
    for fp in dp[2]:
        try:
            with Image.open(dp[0] + '/' + fp, 'r') as im:
                avg = [0, 0, 0]
                total = 0
                hue = 0
                if im.getcolors():
                    for num, color in im.getcolors():
                        if color not in [0, (0, 0, 0), (0, 0, 0, 0), (255, 255, 255), (255, 255, 255, 255)]:
                            try:
                                avg[0] += color[0] * num
                                avg[1] += color[1] * num
                                avg[2] += color[2] * num
                                total += num
                            except TypeError:
                                index = color * 3
                                palette = im.getpalette()
                                avg[0] += palette[index]
                                avg[1] += palette[index + 1]
                                avg[2] += palette[index + 2]
                                total += num
                else:
                    for i in range(im.size[0]):
                        for j in range(im.size[1]):
                            px = im.getpixel((i,j))
                            if len(px) == 3 or px[3] != 0:
                                avg[0] += px[0]
                                avg[1] += px[1]
                                avg[2] += px[2]
                                total += 1
            if total != 0:
                avg[0] /= total
                avg[1] /= total
                avg[2] /= total
            mx = max(avg)
            mn = min(avg)
            try:
                if (mx == avg[0]):
                    #print('red')
                    hue = (avg[1]-avg[2])/(mx-mn)
                elif (mx == avg[1]):
                    #print('green')
                    hue = 2.0 + (((avg[2]-avg[0]))/(mx-mn))
                else:
                    #print('blue')
                    hue = 4.0 + (((avg[0]-avg[1]))/(mx-mn))
            except ZeroDivisionError:
                hue = 0
            hue = ((hue * 60.0 + 360.0) % 360.0) / 360.0
            print(fp, hue)
            hueavg[fp] = hue
        except KeyError as e:
            print(fp, e)

with open("./customization.json", 'r') as jsonf:
    js = json.load(jsonf)
    for dragon_type in js.keys():
        if dragon_type == "defaults":
            continue
        for layer, v in js[dragon_type]['layers'].items():
            for item in v:
                item['average_hue'] = hueavg[item['texture'].split('/')[-1]]

with open("./customization.json", 'w') as f:
    json.dump(js, f, indent=4)
