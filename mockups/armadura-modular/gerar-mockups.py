#!/usr/bin/env python3
# Mockups: módulos de armadura + GUIs no estilo UFO Future (v2)
from PIL import Image, ImageDraw, ImageFont
import random, os

OUT = "/home/raishxn/MineProjects/UFO-Future-1.21.1/mockups/armadura-modular"
os.makedirs(OUT, exist_ok=True)

FONT_PATH = "/home/raishxn/.local/share/fonts/JetBrainsMono/JetBrainsMonoNL-Medium.ttf"
def font(sz):
    try: return ImageFont.truetype(FONT_PATH, sz)
    except Exception: return ImageFont.truetype("/usr/share/fonts/TTF/DejaVuSans-Bold.ttf", sz)

NAVY_BG    = (7, 11, 34, 255)
SLOT_FILL  = (13, 22, 64, 255)
SLOT_BORD  = (42, 60, 140, 255)
SLOT_HI    = (26, 42, 102, 255)
SLOT_SHAD  = (4, 7, 26, 255)
VIOLET     = (180, 91, 255, 255)
VIOLET_D   = (74, 30, 134, 255)
CYAN       = (39, 255, 255, 255)
TEXT       = (232, 244, 255, 255)
TEXT_DIM   = (150, 165, 210, 255)
WHITE      = (244, 248, 255, 255)
GRAY_OFF   = (51, 58, 85, 255)

FAM = {
    "energia":   dict(a=(111,168,255,255), d=(18,29,170,255),  name="ENERGIA"),
    "utilidade": dict(a=(106,255,246,255), d=(40,94,189,255),  name="UTILIDADE"),
    "movimento": dict(a=(255,222,107,255), d=(201,146,26,255), name="MOVIMENTO"),
    "combate":   dict(a=(240,110,240,255), d=(96,20,98,255),   name="COMBATE"),
    "ae2":       dict(a=(179,136,255,255), d=(91,77,201,255),  name="AE2"),
    "capstone":  dict(a=(255,90,90,255),   d=(139,26,26,255),  name="SINGULARIDADE"),
}

P = {
"capacitor_core":["..AAAA..","..AWWA..",".AWWWWA.",".AWWWWA.",".AWWWWA.",".AWWWWA.",".AAAAAA.","........"],
"ae_link":["A..AA..A",".A.AA.A.","..AWWA..",".AAWWAA.","AAWWWWAA",".AAWWAA.","..AWWA..",".A.AA.A."],
"flux_intake":["....AA..","...AA...","..AA....",".AAAAA..","...AA...","..AA....",".AA.....",".A......"],
"storm_harvester":["..AAAA..",".AWWWWA.","AWWWWWWA","........","...AA...","..AAA...","..AA....",".AA....."],
"graviton_flight":["...AA...","..AAWA..",".AA.W.AA","AA..W..A","........","...AA...","..AAWA..",".AA.W.AA"],
"astral_wings":["A.....A.","AA...AA.","AWA.AWA.","AWWA.WWA",".AWWWWA.","..AWWA..","...AA...","........"],
"kinetic_servos":["...AA...","...AWA..","...AWA..","...AWWA.","...AWWA.","..AWWAAA","..AAAAA.",".A..A..A"],
"shock_absorber":["...AA...","...AA...","...AA...",".AAAAAA.","...AA...","..AAAA..","...AA...","..AAAA.."],
"gravity_step":["....AAA.","....AWA.","....AAA.",".AAAAAA.",".AAWAAA.",".AAAAAA.","AAAAAAAA","AAAAAAAA"],
"singularity_aegis":[".AAAAAA.","AWWWWWWA","AWWAWWWA","AWAAAWWA","AWWAWWWA",".AWWWWA.",".AAAAAA.","...AA..."],
"death_denial":[".AA..AA.","AWWAAWWA","AWWWWWWA","AWWWWWWA",".AWWWWA.","..AWWA..","...AA...","........"],
"thorns_field":["A..A..A.",".AAWAA..","AAWWWWAA",".AWWWWA.","AAWWWWAA",".AAWAA..","A..A..A.","........"],
"ferrofluid_weave":[".AAA....","A.WA....","A.WAAA..","A.WA.AA.",".AAAWAA.","...AWWA.","...AWA..","...AAA.."],
"tool_auto_charge":[".A.AA.A.",".AAAAA..","AAAWAAA.","AAWWAAA.","AAAWAAA.",".AAAAA..",".A.AA.A.","........"],
"magnet_core":[".AA..AA.","AWWAAWWA","AWW..WWA","AWW..WWA","AWWWWWWA",".AWWWWA.","..AAAA..",".DD..DD."],
"auto_feed":["...AA...","..A.....",".AWWAA..","AWWWWWA.","AWWWWWA.","AWWWWWA.",".AWWWWA.","..AWWA.."],
"miners_overdrive":["..AAAA..",".AA..AA.","AA....AA","....W...","...WW...","..WW....",".WW.....","WW......"],
"pyro_barrier":["....A...","...AA...","..AWA...",".AWWWA..",".AWWWWA.",".AWWWWA.","..AWWA..","...AA..."],
"dma_thermal_shield":["...AAA..","...AWA..","...AWA..","...AWA..","..AWWWA.","..AWWWA.","...AWWA.","...AAA.."],
"rad_scrubber":["...A....","...A....",".AAWAA..","AWWWWWA.",".AAWAA..","...A....","...A....","..AAA..."],
"spatial_anchor":["...AA...","..AWWA..",".AWWWWA.","AWWWWWWA",".AWWWWA.","..AWWA..","...AA...",".A..A..A"],
"void_swimmer":["..A..A..",".A.AA.A.","..A..A..","........","WW..WW..","W..WW..W","W.W..W.W",".WW..WW."],
"builders_reach":["WW......","WWW.....",".WWW....","..WWW...","...WWW..","....WWWA",".....WWA",".....AAA"],
"wireless_me_access":[".AAAAAA.",".AWWWWA.",".AWAAWA.",".AWWWWA.",".AAAAAA.","...AA...","A..AA..A",".AA..AA."],
"stock_sentinel":[".AAAAAA.",".A....A.",".AWWWWA.",".AWW.AA.",".A.AWA..",".A...AA.",".AAAAAA.","........"],
}

LABELS = {
"capacitor_core":"Capacitor Core","ae_link":"AE Link","flux_intake":"Flux Intake","storm_harvester":"Storm Harvester",
"graviton_flight":"Graviton Flight","astral_wings":"Astral Wings","kinetic_servos":"Kinetic Servos","shock_absorber":"Shock Absorber",
"gravity_step":"Gravity Step","singularity_aegis":"Singularity Aegis","death_denial":"Death Denial","thorns_field":"Thorns Field",
"ferrofluid_weave":"Ferrofluid Weave","tool_auto_charge":"Tool Auto-Charge","magnet_core":"Magnet Core","auto_feed":"Auto-Feed",
"miners_overdrive":"Miner's Overdrive","pyro_barrier":"Pyro Barrier","dma_thermal_shield":"DMA Thermal Shield","rad_scrubber":"Rad Scrubber",
"spatial_anchor":"Spatial Anchor","void_swimmer":"Void Swimmer","builders_reach":"Builder's Reach","wireless_me_access":"Wireless ME Access",
"stock_sentinel":"Stock Sentinel",
}
FAMILY_OF = {
"capacitor_core":"energia","flux_intake":"energia","storm_harvester":"energia",
"ae_link":"ae2","wireless_me_access":"ae2","stock_sentinel":"ae2",
"graviton_flight":"movimento","astral_wings":"movimento","kinetic_servos":"movimento","shock_absorber":"movimento","gravity_step":"movimento",
"singularity_aegis":"capstone","death_denial":"capstone","thorns_field":"combate","ferrofluid_weave":"combate","tool_auto_charge":"combate",
"magnet_core":"utilidade","auto_feed":"utilidade","miners_overdrive":"utilidade","pyro_barrier":"utilidade","dma_thermal_shield":"utilidade",
"rad_scrubber":"utilidade","spatial_anchor":"utilidade","void_swimmer":"utilidade","builders_reach":"utilidade",
}
MK = {k:3 for k in LABELS}
for k in ["gravity_step","thorns_field","ferrofluid_weave","pyro_barrier","dma_thermal_shield","void_swimmer"]: MK[k]=2

STARS = [(3,3),(12,4),(5,12),(11,11),(8,2),(2,8)]

def draw_card(name, fam, mk, S=1):
    f = FAM[fam]; im = Image.new("RGBA",(16*S,16*S),(0,0,0,0))
    d = ImageDraw.Draw(im)
    def px(x,y,c): d.rectangle([x*S,y*S,x*S+S-1,y*S+S-1],fill=c)
    for y in range(1,15):
        for x in range(1,15):
            t = y/15.0
            px(x,y,(int(10+t*6), int(14+t*8), int(48+t*26), 255))
    for i in range(1,15):
        px(i,1,(35,43,94,255)); px(i,14,(20,26,66,255))
        px(1,i,(30,38,84,255)); px(14,i,(24,31,74,255))
    for i,(sx,sy) in enumerate(STARS):
        px(sx,sy,(255,255,255,110+i*12)) if i%2 else px(sx,sy,(160,240,255,120))
    for (cx,cy,dx,dy) in [(1,1,1,1),(13,1,-1,1),(1,13,1,-1),(13,13,-1,-1)]:
        for k in range(4):
            px(cx+dx*k, cy, f["a"]); px(cx, cy+dy*k, f["a"])
        px(cx+dx, cy+dy, f["d"])
    m = P[name]; ox, oy = 4, 4
    for ry,row in enumerate(m):
        for rx,ch in enumerate(row):
            if ch==".": continue
            px(ox+rx, oy+ry, WHITE if ch=="W" else (f["d"] if ch=="D" else f["a"]))
    for n in range(3):
        px(5+n*3, 15, f["a"] if n < mk else (35,43,94,255))
    return im

def card_sheet():
    S=8; cols=6; cell=16*S; pad=12; lab=16
    names=list(LABELS); rows=(len(names)+cols-1)//cols
    head=196
    W=1000
    H=head+rows*(cell+lab+pad)+pad
    canvas=Image.new("RGBA",(W,H),(24,24,32,255))
    d=ImageDraw.Draw(canvas)
    d.text((pad,14),"UFO FUTURE — MÓDULOS DE ARMADURA (MOCKUP)",font=font(30),fill=VIOLET)
    d.text((pad,56),"Base própria: roseta dos catalysts · brackets de canto + núcleo estelar · notches MK1–MK3 na base",font=font(16),fill=TEXT_DIM)
    d.text((pad,80),"azul=energia · cyan=utilidade · dourado=movimento · magenta=combate · violeta=AE2 · vermelho=singularidade",font=font(16),fill=TEXT_DIM)
    d.text((pad,112),"Referência analisada (AdvancedAE):",font=font(16),fill=TEXT_DIM)
    aae=[Image.open(p).convert("RGBA") for p in [
        "/tmp/advancedae-ref/src/main/resources/assets/advanced_ae/textures/item/upgrades/flight_card.png",
        "/tmp/advancedae-ref/src/main/resources/assets/advanced_ae/textures/item/upgrades/magnet_card.png",
        "/tmp/advancedae-ref/src/main/resources/assets/advanced_ae/textures/item/upgrades/recharging_card.png"]]
    for i,card in enumerate(aae):
        c=card.resize((64,64),Image.NEAREST); x=pad+i*220; y=134
        canvas.paste(c,(x,y),c)
        d.text((x+70,y+8),["flight","magnet","recharging"][i],font=font(14),fill=TEXT_DIM)
    d.text((pad+700,y+20),"→ reimaginado abaixo",font=font(16),fill=CYAN)
    for idx,n in enumerate(names):
        r,c=divmod(idx,cols)
        x=pad+c*(cell+pad); y=head+r*(cell+lab+pad)
        fam=FAMILY_OF[n]
        card=draw_card(n,fam,MK[n]).resize((cell,cell),Image.NEAREST)
        canvas.paste(card,(x,y),card)
        d.text((x,y+cell+1),LABELS[n],font=font(12),fill=FAM[fam]["a"])
    canvas.save(f"{OUT}/modulos-cards-sheet.png"); print("ok cards",canvas.size)

class Gui:
    def __init__(self,w,h,S=4):
        self.S=S; self.w=w; self.h=h
        self.im=Image.new("RGBA",(w*S,h*S)); self.d=ImageDraw.Draw(self.im)
        rnd=random.Random(7)
        for _ in range(int(w*h/95)):
            x,y=rnd.randrange(w),rnd.randrange(h)
            c=rnd.choice([(255,255,255,150),(160,240,255,140),(200,150,255,130),(255,255,255,80)])
            self.d.rectangle([x*S,y*S,x*S+1,y*S+1],fill=c)
        self.d.rectangle([0,0,w*S-1,h*S-1],outline=VIOLET_D,width=2*S)
        self.d.rectangle([S,S,(w-1)*S-S,(h-1)*S-S],outline=VIOLET,width=S)
        self.d.rectangle([2*S,2*S,(w-3)*S-S,(h-3)*S-S],outline=(40,18,80,255),width=S)
    def rect(self,x,y,w,h,fill,outline=None):
        self.d.rectangle([x*self.S,y*self.S,(x+w)*self.S-1,(y+h)*self.S-1],fill=fill,outline=outline)
    def text(self,x,y,s,c=TEXT,sz=13,bold=False):
        self.d.text((x*self.S,y*self.S),s,font=font(sz*(self.S//4)+(6 if bold else 0)),fill=c)
    def slot(self,x,y):
        self.rect(x,y,18,18,SLOT_FILL,SLOT_BORD)
        self.d.line([x*self.S,y*self.S,(x+17)*self.S,y*self.S],fill=SLOT_SHAD,width=self.S)
        self.d.line([x*self.S,y*self.S,x*self.S,(y+17)*self.S],fill=SLOT_SHAD,width=self.S)
        self.d.line([x*self.S,(y+17)*self.S+1,(x+17)*self.S,(y+17)*self.S+1],fill=SLOT_HI,width=1)
    def toggle(self,x,y,on):
        c=CYAN if on else GRAY_OFF
        self.rect(x,y,16,8,(10,20,40,255),c)
        self.text(x+3,y+1,("ON" if on else "OFF"),c if on else TEXT_DIM,sz=10)
    def gear(self,x,y,c=TEXT_DIM):
        self.rect(x+1,y+1,5,5,None,c); self.rect(x+2,y+2,3,3,None,c)
        for dx,dy in [(2,0),(2,6),(0,2),(6,2)]: self.rect(x+dx,y+dy,1,1,c)
    def xbtn(self,x,y):
        for i in range(5):
            self.rect(x+i,y+i,1,1,TEXT_DIM); self.rect(x+4-i,y+i,1,1,TEXT_DIM)

ARMOR_PARTS = {"helmet":"helmet.png","chestplate":"ufo_chestplate.png","leggings":"ufo_leggings.png","boots":"ufo_boots.png"}
ITEM_DIR="/home/raishxn/MineProjects/UFO-Future-1.21.1/src/main/resources/assets/ufo/textures/item"

def paste_icon(gui, im16, x, y):
    im=im16.resize((16*gui.S,16*gui.S),Image.NEAREST); gui.im.paste(im,(x*gui.S,y*gui.S),im)

def gui_bench():
    g=Gui(232,196)
    g.text(8,6,"UFO ARMOR — BANCADA DE MÓDULOS",CYAN,sz=15,bold=True)
    g.rect(150,4,38,12,(20,30,80,255),CYAN); g.text(154,7,"MÓDULOS",CYAN,sz=11)
    g.rect(190,4,34,12,(12,16,44,255),SLOT_BORD); g.text(194,7,"AJUSTES",TEXT_DIM,sz=11)
    g.d.line([8*g.S,20*g.S,224*g.S,20*g.S],fill=(40,18,80,255),width=2)
    g.text(8,24,"ARMADURA",TEXT_DIM,sz=11)
    y=34
    for part in ["helmet","chestplate","leggings","boots"]:
        g.slot(8,y)
        p=f"{ITEM_DIR}/{ARMOR_PARTS[part]}"
        if os.path.exists(p): paste_icon(g,Image.open(p).convert("RGBA"),9,y+1)
        y+=20
    ex,ey=30,34
    g.rect(ex-1,ey-1,10,66,(4,7,26,255),SLOT_BORD)
    for i in range(15):
        t=i/14.0
        g.rect(ex,ey+60-i*4,8,4,(int(139+t*(39-139)),int(47+t*(255-47)),int(214+t*(255-214)),255))
    g.rect(ex,ey,8,4,CYAN)
    g.text(ex+11,ey+52,"82%",TEXT_DIM,sz=10)
    g.text(8,118,"1,2B / 100B RF",TEXT_DIM,sz=10)
    g.text(8,130,"MK3 · kit completo",TEXT_DIM,sz=10)
    g.text(52,24,"MÓDULOS (6)",TEXT_DIM,sz=11)
    order=["singularity_aegis","ae_link","graviton_flight","astral_wings","magnet_core","capacitor_core"]
    pos=[(52,34),(72,34),(92,34),(52,54),(72,54),(92,54)]
    for n,(x,y) in zip(order,pos):
        g.slot(x,y); paste_icon(g,draw_card(n,FAMILY_OF[n],MK[n]),x+1,y+1)
    g.text(52,76,"→ SAÍDA",TEXT_DIM,sz=11)
    g.slot(92,74); paste_icon(g,draw_card("ae_link","ae2",3),93,75)
    g.text(52,98,"TIER DO SLOT",TEXT_DIM,sz=11)
    for i,mk in enumerate(["MK1","MK2","MK3"]):
        x=52+i*18
        on=i==2
        g.rect(x,108,16,10,(20,30,80,255) if on else (12,16,44,255),CYAN if on else SLOT_BORD)
        g.text(x+2,110,mk,CYAN if on else TEXT_DIM,sz=10)
    g.text(52,124,"Custo: 64x Neutron Star",TEXT_DIM,sz=10)
    g.text(52,134,"+ Event Horizon Proc.",TEXT_DIM,sz=10)
    g.rect(116,24,108,132,(10,16,48,255),(42,60,140,255))
    g.text(120,26,"INSTALADOS",TEXT_DIM,sz=11)
    rows=[("singularity_aegis",True),("ae_link",True),("graviton_flight",True),("astral_wings",False),("magnet_core",True),("stock_sentinel",False)]
    ry=40
    for n,on in rows:
        fam=FAMILY_OF[n]
        paste_icon(g,draw_card(n,fam,MK[n]),119,ry)
        g.text(137,ry+1,LABELS[n][:17],TEXT,sz=11)
        g.text(137,ry+9,FAM[fam]["name"],FAM[fam]["a"],sz=9)
        g.toggle(190,ry+4,on)
        g.xbtn(212,ry+5)
        if ry>40: g.d.line([119*g.S,(ry-2)*g.S,216*g.S,(ry-2)*g.S],fill=(30,40,90,255),width=1)
        ry+=19
    g.rect(219,28,3,124,(6,10,32,255),SLOT_BORD)
    g.rect(219,28,3,40,(30,44,110,255),CYAN)
    g.text(8,160,"INVENTÁRIO",TEXT_DIM,sz=11)
    for i in range(9): g.slot(8+i*19,170)
    g.text(120,164,"Scroll: modo do módulo · Shift+Clique: remover",TEXT_DIM,sz=10)
    g.im.resize((g.im.width*2,g.im.height*2),Image.NEAREST).save(f"{OUT}/gui-bancada-modulos.png")
    print("ok bench")

def gui_config():
    g=Gui(196,158)
    g.text(8,6,"ARMADURA UFO — AJUSTES",CYAN,sz=15,bold=True)
    y=26
    for part in ["helmet","chestplate","leggings","boots"]:
        g.slot(8,y)
        p=f"{ITEM_DIR}/{ARMOR_PARTS[part]}"
        if os.path.exists(p): paste_icon(g,Image.open(p).convert("RGBA"),9,y+1)
        y+=20
    g.rect(32,24,156,122,(10,16,48,255),(42,60,140,255))
    rows=[("singularity_aegis","Reflect: OFF",True),("ae_link","Priorid.: AE",True),
          ("graviton_flight","Veloc.: 200%",True),("magnet_core","Raio: 16",False),
          ("stock_sentinel","3 alvos",False)]
    ry=30
    for n,cfg,on in rows:
        fam=FAMILY_OF[n]
        paste_icon(g,draw_card(n,fam,MK[n]),36,ry)
        g.text(54,ry+1,LABELS[n],TEXT,sz=11)
        g.text(54,ry+10,cfg,FAM[fam]["a"],sz=9)
        g.gear(146,ry+4); g.toggle(156,ry+4,on); g.xbtn(178,ry+5)
        if ry>30: g.d.line([36*g.S,(ry-2)*g.S,182*g.S,(ry-2)*g.S],fill=(30,40,90,255),width=1)
        ry+=22
    g.text(8,150,"configurado na armadura · o servidor valida os limites",TEXT_DIM,sz=9)
    g.im.resize((g.im.width*2,g.im.height*2),Image.NEAREST).save(f"{OUT}/gui-ajustes-ingame.png")
    print("ok config")

card_sheet()
gui_bench()
gui_config()
