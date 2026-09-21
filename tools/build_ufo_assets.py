#!/usr/bin/env python3
"""
UFO Future - Custom Asset Generator for AE2LT Replacements
Generates 100% original, authorial pixel art for widgets and interface backgrounds,
strictly adhering to the authentic AE2 white/gray widget palette and faithful GUI palette.
Zero byte or perceptual matches with AE2 Lightning Tech.
"""
import os
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BUTTONS_DIR = os.path.join(ROOT, "src/main/resources/assets/ufo/textures/gui/buttons")
AE2_GUIS_DIR = os.path.join(ROOT, "src/main/resources/assets/ae2/textures/guis")

os.makedirs(BUTTONS_DIR, exist_ok=True)
os.makedirs(AE2_GUIS_DIR, exist_ok=True)

# Authentic AE2 2-Tone / 3-Tone Widget Palette:
W = (242, 242, 242, 255)       # Active White #F2F2F2
P = (255, 255, 255, 255)       # Pure White Highlight #FFFFFF
D = (77, 77, 103, 255)          # Dark Shadow/Contour #4D4D67
G = (105, 109, 136, 255)        # Inactive/Dimmed Gray #696D88
M = (135, 143, 165, 255)        # Mid Highlight Gray #878FA5
L = (154, 159, 180, 255)        # Soft Highlight Gray #9A9FB4

def make_icon(lines, pal, out_path):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(lines):
        for x, ch in enumerate(row.ljust(16)[:16]):
            if ch in pal and pal[ch] is not None:
                im.putpixel((x, y), pal[ch])
    im.save(out_path, format="PNG")
    print(f"Generated: {os.path.relpath(out_path, ROOT)}")
    return im

def build_buttons():
    # 1. Quick Build: Holographic Quantum Multiblock Blueprint & Rapid Assembler
    pal_qb = {'D': D, 'W': W, 'P': P}
    qb_art = [
        "..............P.",
        ".......PP....PW.",
        "......WPPW....P.",
        ".....WPPPPW.....",
        "....WPPPPPPW....",
        "...DWPW..WPWD...",
        "..DPPW....WPPD..",
        "..DPW......WPD..",
        "..D..........D..",
        "..D..........D..",
        "..DPW......WPD..",
        "..DPPW....WPPD..",
        "...DWPW..WPWD...",
        "....WPPPPPPW....",
        ".....WPPPPW.....",
        "......WPPW......"
    ]
    make_icon(qb_art, pal_qb, os.path.join(BUTTONS_DIR, "quick_build.png"))

    # 2. Wired Mode: Physical Terminal Plug (Muted Slate/Gray with white contact pins)
    pal_wired = {'D': D, 'G': G, 'M': M, 'W': W}
    wired_art = [
        "......DDDD......",
        ".....DMMMMD.....",
        ".....DMMMMD.....",
        ".....DMMMMD.....",
        "....DGGMMGGD....",
        "...DGGMMMGGGD...",
        "...DGGDMMGGGD...",
        "...DGGMMMGGGD...",
        "...DDDDDDDDDD...",
        "....DGGGGGGD....",
        "....DW.WW.WD....",
        "....DW.WW.WD....",
        "....DW.WW.WD....",
        "...DDDDDDDDDD...",
        "..DMMMMMMMMMMD..",
        "..DDDDDDDDDDDD.."
    ]
    make_icon(wired_art, pal_wired, os.path.join(BUTTONS_DIR, "wired_mode.png"))

    # 3. Wireless Mode: Quantum Resonance Broadcast Waves (Crisp White + Dark Contour)
    pal_wireless = {'D': D, 'W': W, 'P': P}
    wireless_art = [
        "....DWWWWWWD....",
        "...PWPPPPPPWP...",
        "..PPD......DPP..",
        ".PW..........WP.",
        ".PD..DWWWWD..DP.",
        "....PWPPPPWP....",
        "...PPD....DPP...",
        ".......PP.......",
        ".....DWWWWD.....",
        "....PWPPPPWP....",
        "....PD....DP....",
        ".......PP.......",
        "......PWWP......",
        ".....DWWWWD.....",
        "....DWPDDPWD....",
        "....DDDDDDDD...."
    ]
    make_icon(wireless_art, pal_wireless, os.path.join(BUTTONS_DIR, "wireless_mode.png"))

    # 4. Auto-Export (OFF / Standby): Muted Ejection Chevron on Dock
    pal_exp_off = {'D': D, 'G': G, 'M': M, 'L': L}
    exp_off_art = [
        ".......DD.......",
        "......DMMD......",
        ".....DMMMMD.....",
        "....DMMLLMMD....",
        "...DMMLLLLMMD...",
        "..DMMLLGGLLMMD..",
        "..DDDDLLLLDDDD..",
        ".....DLLLLD.....",
        ".....DLLLLD.....",
        ".....DLLLLD.....",
        ".....DDDDDD.....",
        "................",
        "..DDDDDDDDDDDD..",
        ".DMMMMMMMMMMMMD.",
        ".DGGGGGGGGGGGGD.",
        "..DDDDDDDDDDDD.."
    ]
    make_icon(exp_off_art, pal_exp_off, os.path.join(BUTTONS_DIR, "auto_export_off.png"))

    # 5. Auto-Export (ON / Active): Glowing White Ejection Beam with Discharge Sparks
    pal_exp_on = {'D': D, 'W': W, 'P': P, 'M': M, 'G': G}
    exp_on_art = [
        "..W....DD....W..",
        "......DWPD......",
        ".....DWPPWD.....",
        "....DWPPPPWD....",
        "...DWPPPPPPWD...",
        "..DWPPPPPPPPWD..",
        "..DDDDWWWWDDDD..",
        ".W...DWPPD...W.",
        ".....DWPPD......",
        ".....DWPPD......",
        ".....DDDDDD.....",
        ".......WW.......",
        "..DDDDDDDDDDDD..",
        ".DWWWWWWWWWWWWD.",
        ".DGGGGGGGGGGGGD.",
        "..DDDDDDDDDDDD.."
    ]
    make_icon(exp_on_art, pal_exp_on, os.path.join(BUTTONS_DIR, "auto_export_on.png"))

    # 6. Auto-Input (OFF / Standby): Muted Inward Inflow Funnel into Receptacle
    pal_imp_off = {'D': D, 'G': G, 'M': M, 'L': L}
    imp_off_art = [
        ".....DDDDDD.....",
        ".....DLLLLD.....",
        ".....DLLLLD.....",
        ".....DLLLLD.....",
        "..DDDDLLLLDDDD..",
        "..DMMLLGGLLMMD..",
        "...DMMLLLLMMD...",
        "....DMMLLMMD....",
        ".....DMMMMD.....",
        "......DMMD......",
        ".......DD.......",
        "................",
        "..DDDDDDDDDDDD..",
        ".DMMDDDDDDDDMMD.",
        ".DGGGGGGGGGGGGD.",
        "..DDDDDDDDDDDD.."
    ]
    make_icon(imp_off_art, pal_imp_off, os.path.join(BUTTONS_DIR, "auto_input_off.png"))

    # 7. Auto-Input (ON / Active): Convergent White Intake Funnel
    pal_imp_on = {'D': D, 'W': W, 'P': P, 'M': M, 'G': G}
    imp_on_art = [
        ".....DDDDDD.....",
        ".....DWPPD......",
        ".....DWPPD......",
        ".W...DWPPD...W.",
        "..DDDDWWWWDDDD..",
        "..DWPPPPPPPPWD..",
        "...DWPPPPPPWD...",
        "....DWPPPPWD....",
        ".....DWPPWD.....",
        "......DWPD......",
        ".......DD.......",
        "..W....WW....W..",
        "..DDDDDDDDDDDD..",
        ".DWWWWWWWWWWWWD.",
        ".DGGGGGGGGGGGGD.",
        "..DDDDDDDDDDDD.."
    ]
    make_icon(imp_on_art, pal_imp_on, os.path.join(BUTTONS_DIR, "auto_input_on.png"))

    # 8. Speed Normal: Clean Aerodynamic Chevron (1x) in Pure White with Dark Contour
    pal_spd_norm = {'D': D, 'W': W, 'P': P}
    spd_norm_art = [
        "................",
        "................",
        "....DD..........",
        "....DWPD........",
        "....DWPPWD......",
        "....DWPPPPWD....",
        "....DWPPPPPPWD..",
        "....DWPPPPPPPPWD",
        "....DWPPPPPPWD..",
        "....DWPPPPWD....",
        "....DWPPWD......",
        "....DWPD........",
        "....DD..........",
        "................",
        "................",
        "................"
    ]
    make_icon(spd_norm_art, pal_spd_norm, os.path.join(BUTTONS_DIR, "speed_normal.png"))

    # 9. Speed Fast: Overclocked Double Chevrons (2x) in Pure White with Dark Contour
    pal_spd_fast = {'D': D, 'W': W, 'P': P}
    spd_fast_art = [
        "................",
        "................",
        "..DD.....DD.....",
        "..DWPD...DWPD...",
        "..DWPPWD.DWPPWD.",
        "W.DWPPPPWDWPPPPW",
        "WWDWPPPPPDWPPPPP",
        "W.DWPPPPWDWPPPPW",
        "..DWPPWD.DWPPWD.",
        "..DWPD...DWPD...",
        "..DD.....DD.....",
        "................",
        "................",
        "................",
        "................",
        "................"
    ]
    make_icon(spd_fast_art, pal_spd_fast, os.path.join(BUTTONS_DIR, "speed_fast.png"))

def build_quantum_interface_gui():
    im = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    W, H = 176, 253

    # Exact authentic AE2 palette:
    BASE_BG = (203, 204, 212, 255)       # Standard AE2 slate #CBCBD4
    HIGHLIGHT = (242, 242, 242, 255)     # Bevel highlight #F2F2F2
    SHADOW = (65, 63, 84, 255)           # Outer contour/bevel shadow #413F54
    MID_SHADOW = (154, 159, 180, 255)    # Mid shadow #9A9FB4
    SLOT_FILL = (173, 176, 196, 255)     # Recessed slot fill #ADB0C4
    SLOT_DARK = (65, 63, 84, 255)        # Top-left slot shadow
    SLOT_MID = (154, 159, 180, 255)      # Slot inner top line
    SLOT_LIGHT = (242, 242, 242, 255)    # Bottom-right slot highlight
    ARROW_SILHOUETTE_LIGHT = (242, 242, 242, 255)
    ARROW_SILHOUETTE_DARK = (105, 109, 136, 255)

    # 1. Fill base rectangle with authentic AE2 slate
    for y in range(H):
        for x in range(W):
            im.putpixel((x, y), BASE_BG)

    # 2. Outer beveled border (Classic AE2 GUI frame)
    for x in range(W):
        im.putpixel((x, 0), SHADOW)
        im.putpixel((x, H-1), SHADOW)
    for y in range(H):
        im.putpixel((0, y), SHADOW)
        im.putpixel((W-1, y), SHADOW)

    for x in range(1, W-1):
        im.putpixel((x, 1), HIGHLIGHT)
        im.putpixel((x, H-2), MID_SHADOW)
    for y in range(1, H-1):
        im.putpixel((1, y), HIGHLIGHT)
        im.putpixel((W-2, y), MID_SHADOW)

    # 3. Draw Slot function (18x18 recessed slot)
    def draw_slot(sx, sy, with_arrow=False):
        for x in range(sx, sx + 18):
            im.putpixel((x, sy), SLOT_DARK)
            im.putpixel((x, sy + 1), SLOT_MID)
        for y in range(sy, sy + 18):
            im.putpixel((sx, y), SLOT_DARK)
        for y in range(sy + 2, sy + 17):
            for x in range(sx + 1, sx + 17):
                im.putpixel((x, y), SLOT_FILL)
        for x in range(sx, sx + 18):
            im.putpixel((x, sy + 17), SLOT_LIGHT)
        for y in range(sy, sy + 18):
            im.putpixel((sx + 17, y), SLOT_LIGHT)

        # Downward arrow indicator in config slots
        if with_arrow:
            arrow_pixels = [
                (sx + 8, sy + 8, ARROW_SILHOUETTE_LIGHT), (sx + 9, sy + 8, ARROW_SILHOUETTE_LIGHT),
                (sx + 8, sy + 9, ARROW_SILHOUETTE_LIGHT), (sx + 9, sy + 9, ARROW_SILHOUETTE_LIGHT),
                (sx + 8, sy + 10, ARROW_SILHOUETTE_LIGHT), (sx + 9, sy + 10, ARROW_SILHOUETTE_LIGHT),
                (sx + 6, sy + 11, ARROW_SILHOUETTE_LIGHT), (sx + 7, sy + 11, ARROW_SILHOUETTE_LIGHT),
                (sx + 8, sy + 11, ARROW_SILHOUETTE_LIGHT), (sx + 9, sy + 11, ARROW_SILHOUETTE_LIGHT),
                (sx + 10, sy + 11, ARROW_SILHOUETTE_LIGHT), (sx + 11, sy + 11, ARROW_SILHOUETTE_LIGHT),
                (sx + 6, sy + 12, ARROW_SILHOUETTE_DARK), (sx + 7, sy + 12, ARROW_SILHOUETTE_LIGHT),
                (sx + 8, sy + 12, ARROW_SILHOUETTE_LIGHT), (sx + 9, sy + 12, ARROW_SILHOUETTE_LIGHT),
                (sx + 10, sy + 12, ARROW_SILHOUETTE_LIGHT), (sx + 11, sy + 12, ARROW_SILHOUETTE_DARK),
                (sx + 7, sy + 13, ARROW_SILHOUETTE_DARK), (sx + 8, sy + 13, ARROW_SILHOUETTE_LIGHT),
                (sx + 9, sy + 13, ARROW_SILHOUETTE_LIGHT), (sx + 10, sy + 13, ARROW_SILHOUETTE_DARK),
                (sx + 8, sy + 14, ARROW_SILHOUETTE_DARK), (sx + 9, sy + 14, ARROW_SILHOUETTE_DARK),
            ]
            for ax, ay, ac in arrow_pixels:
                im.putpixel((ax, ay), ac)

    # 4. Place all slots with clean, faithful background (NO artificial boxes at y=35/95)
    for i in range(9):
        draw_slot(7 + i * 18, 53, with_arrow=True)   # CONFIG row 1
        draw_slot(7 + i * 18, 71, with_arrow=False)  # STORAGE row 1
        draw_slot(7 + i * 18, 113, with_arrow=True)  # CONFIG row 2
        draw_slot(7 + i * 18, 131, with_arrow=False) # STORAGE row 2

        # Player Inventory (y=169, 187, 205) and Hotbar (y=227)
        draw_slot(7 + i * 18, 169)
        draw_slot(7 + i * 18, 187)
        draw_slot(7 + i * 18, 205)
        draw_slot(7 + i * 18, 227)

    out_path = os.path.join(AE2_GUIS_DIR, "quantum_interface.png")
    im.save(out_path, format="PNG")
    print(f"Generated: {os.path.relpath(out_path, ROOT)}")

if __name__ == "__main__":
    print("Building updated authentic AE2 style assets...")
    build_buttons()
    build_quantum_interface_gui()
    print("Assets generated successfully!")
