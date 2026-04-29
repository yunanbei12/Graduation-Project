from __future__ import annotations

from pathlib import Path
from typing import Iterable

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
ASSET_DIR = ROOT / "docs" / "assets" / "thesis"
ASSET_DIR.mkdir(parents=True, exist_ok=True)

WIDTH = 1600
HEIGHT = 920
BG = "#f5f7fb"
CARD = "#ffffff"
LINE = "#d9e2ef"
TEXT = "#243447"
SUB = "#6b7b8c"
PRIMARY = "#3d7dff"
SUCCESS = "#3bb273"
WARNING = "#f4a259"
DANGER = "#e85d75"


def get_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = [
        "/System/Library/Fonts/PingFang.ttc",
        "/System/Library/Fonts/Hiragino Sans GB.ttc",
        "/System/Library/Fonts/STHeiti Medium.ttc",
        "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
    ]
    for path in candidates:
        try:
            return ImageFont.truetype(path, size=size)
        except OSError:
            continue
    return ImageFont.load_default()


FONT_TITLE = get_font(34, bold=True)
FONT_H2 = get_font(24, bold=True)
FONT_TEXT = get_font(18)
FONT_SMALL = get_font(16)
FONT_TINY = get_font(14)


def draw_tag(draw: ImageDraw.ImageDraw, xy, text, fill, text_fill="white"):
    x1, y1, x2, y2 = xy
    draw.rounded_rectangle(xy, radius=12, fill=fill)
    bbox = draw.textbbox((0, 0), text, font=FONT_TINY)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    draw.text((x1 + (x2 - x1 - tw) / 2, y1 + (y2 - y1 - th) / 2 - 1), text, font=FONT_TINY, fill=text_fill)


def draw_card(draw: ImageDraw.ImageDraw, xy, title=None):
    draw.rounded_rectangle(xy, radius=24, fill=CARD, outline=LINE, width=2)
    if title:
        x1, y1, x2, _ = xy
        draw.text((x1 + 24, y1 + 18), title, font=FONT_H2, fill=TEXT)
        draw.line((x1, y1 + 58, x2, y1 + 58), fill=LINE, width=2)


def draw_button(draw: ImageDraw.ImageDraw, xy, text, primary=False):
    fill = PRIMARY if primary else "#eef3ff"
    text_fill = "white" if primary else PRIMARY
    draw.rounded_rectangle(xy, radius=16, fill=fill, outline=PRIMARY if not primary else None, width=2)
    bbox = draw.textbbox((0, 0), text, font=FONT_SMALL)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    x1, y1, x2, y2 = xy
    draw.text((x1 + (x2 - x1 - tw) / 2, y1 + (y2 - y1 - th) / 2 - 1), text, font=FONT_SMALL, fill=text_fill)


def draw_input(draw: ImageDraw.ImageDraw, xy, label, value="", small=False):
    x1, y1, x2, y2 = xy
    draw.text((x1, y1 - 26), label, font=FONT_SMALL if not small else FONT_TINY, fill=SUB)
    draw.rounded_rectangle(xy, radius=14, fill="#fbfcff", outline=LINE, width=2)
    if value:
        draw.text((x1 + 16, y1 + 12), value, font=FONT_SMALL if not small else FONT_TINY, fill=TEXT)


def draw_table(draw: ImageDraw.ImageDraw, x, y, w, headers: list[str], rows: list[list[str]], col_widths: list[int]):
    row_h = 52
    draw.rounded_rectangle((x, y, x + w, y + row_h), radius=14, fill="#f1f5fb", outline=LINE, width=2)
    cur_x = x
    for idx, header in enumerate(headers):
        cw = col_widths[idx]
        draw.text((cur_x + 14, y + 14), header, font=FONT_SMALL, fill=TEXT)
        cur_x += cw
        if idx < len(headers) - 1:
            draw.line((cur_x, y, cur_x, y + row_h + row_h * len(rows)), fill=LINE, width=2)
    for ridx, row in enumerate(rows):
        top = y + row_h * (ridx + 1)
        draw.rectangle((x, top, x + w, top + row_h), fill=CARD, outline=LINE, width=1)
        cur_x = x
        for cidx, cell in enumerate(row):
            draw.text((cur_x + 14, top + 14), cell, font=FONT_SMALL, fill=TEXT)
            cur_x += col_widths[cidx]


def wrap_lines(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont, width: int) -> Iterable[str]:
    words = list(text)
    current = ""
    for ch in words:
        test = current + ch
        bbox = draw.textbbox((0, 0), test, font=font)
        if bbox[2] - bbox[0] <= width:
            current = test
        else:
            if current:
                yield current
            current = ch
    if current:
        yield current


def draw_paragraph(draw: ImageDraw.ImageDraw, x: int, y: int, text: str, width: int, font=FONT_TINY, fill=SUB, line_gap=8):
    current_y = y
    for line in wrap_lines(draw, text, font, width):
        draw.text((x, current_y), line, font=font, fill=fill)
        bbox = draw.textbbox((0, 0), line, font=font)
        current_y += (bbox[3] - bbox[1]) + line_gap
    return current_y


def base_canvas(title: str, subtitle: str) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = Image.new("RGB", (WIDTH, HEIGHT), BG)
    draw = ImageDraw.Draw(image)
    draw.rounded_rectangle((26, 24, WIDTH - 26, HEIGHT - 24), radius=32, fill="#f7f9fc", outline="#e6ebf3", width=2)
    draw.rounded_rectangle((26, 24, WIDTH - 26, 112), radius=32, fill="#1f2f46")
    draw.rectangle((26, 80, WIDTH - 26, 112), fill="#1f2f46")
    draw.text((56, 42), "KINETIC 管理端", font=FONT_TITLE, fill="white")
    draw.text((56, 84), title, font=FONT_H2, fill="#dce6f6")
    draw.text((WIDTH - 460, 46), subtitle, font=FONT_SMALL, fill="#b7c7df")
    return image, draw


def save(img: Image.Image, name: str):
    img.save(ASSET_DIR / name)


def figure_course():
    img, draw = base_canvas("课程与基础资料管理", "课程、分类、地点、教练等基础信息统一维护")
    draw_card(draw, (56, 146, 1020, 842), "课程管理")
    draw_input(draw, (88, 232, 258, 278), "课程类型", "私教课")
    draw_input(draw, (278, 232, 458, 278), "课程分类", "篮球启蒙")
    draw_button(draw, (796, 226, 910, 278), "地点管理")
    draw_button(draw, (926, 226, 1000, 278), "新增", primary=True)
    headers = ["ID", "课程名称", "类型", "价格", "销量", "状态", "操作"]
    col_widths = [80, 220, 120, 120, 100, 120, 140]
    rows = [
        ["101", "少儿篮球私教", "私教课", "¥299", "86", "上架", "编辑 删除"],
        ["102", "中考体育冲刺", "私教课", "¥399", "54", "上架", "编辑 删除"],
        ["203", "周末体能团课", "团课", "¥99", "132", "上架", "编辑 删除"],
        ["205", "羽毛球基础班", "团课", "¥129", "77", "下架", "编辑 删除"],
    ]
    draw_table(draw, 88, 312, 900, headers, rows, col_widths)

    draw_card(draw, (1060, 146, 1528, 842), "新增课程")
    draw_input(draw, (1090, 232, 1498, 278), "课程名称", "少儿篮球私教")
    draw_input(draw, (1090, 318, 1288, 364), "课程类型", "私教课")
    draw_input(draw, (1300, 318, 1498, 364), "课程分类", "篮球启蒙")
    draw_input(draw, (1090, 404, 1288, 450), "价格", "299.00")
    draw_input(draw, (1300, 404, 1498, 450), "课时数", "12")
    draw_input(draw, (1090, 490, 1288, 536), "有效期(天)", "90")
    draw_input(draw, (1300, 490, 1498, 536), "教练分成(%)", "50")
    draw_input(draw, (1090, 576, 1498, 622), "默认授课教练", "王教练")
    draw_input(draw, (1090, 662, 1498, 708), "是否上门", "支持")
    draw.text((1090, 744), "课程图片 / 描述 / 详情在同一表单中维护", font=FONT_SMALL, fill=SUB)
    draw_button(draw, (1244, 782, 1358, 826), "取消")
    draw_button(draw, (1372, 782, 1498, 826), "保存", primary=True)
    save(img, "fig4-5-course-management.png")


def figure_schedule():
    img, draw = base_canvas("排课管理与教练看板", "围绕团课场次、地点、教练与成团状态进行管理")
    draw_card(draw, (56, 146, 1528, 290), "排课筛选与操作")
    draw_input(draw, (88, 220, 288, 266), "筛选地点", "万达运动馆")
    draw_input(draw, (314, 220, 514, 266), "筛选团课", "周末体能团课")
    draw_button(draw, (1160, 214, 1316, 266), "教练排课看板")
    draw_button(draw, (1332, 214, 1496, 266), "新增排课", primary=True)

    draw_card(draw, (56, 322, 1528, 842), "教练排课看板")
    week_x = 270
    coach_x = 82
    draw.rounded_rectangle((82, 392, 1500, 444), radius=12, fill="#f1f5fb", outline=LINE, width=2)
    draw.text((coach_x, 408), "教练", font=FONT_SMALL, fill=TEXT)
    days = ["周一\n04-21", "周二\n04-22", "周三\n04-23", "周四\n04-24", "周五\n04-25", "周六\n04-26", "周日\n04-27"]
    for i, day in enumerate(days):
        dx = week_x + i * 176
        draw.text((dx, 398), day, font=FONT_TINY, fill=TEXT, align="center")
        draw.line((dx - 18, 392, dx - 18, 792), fill=LINE, width=2)
    coaches = ["王教练", "李教练", "陈教练"]
    for r, coach in enumerate(coaches):
        top = 444 + r * 116
        draw.rectangle((82, top, 1500, top + 116), fill=CARD, outline=LINE, width=1)
        draw.text((coach_x, top + 22), coach, font=FONT_SMALL, fill=TEXT)
        draw.text((coach_x, top + 56), f"{2 + r} 节团课  ·  {18 + r * 4} 人已报", font=FONT_TINY, fill=SUB)
    cards = [
        (0, 0, "16:00-17:00\n体能团课\n18/20", SUCCESS),
        (0, 2, "19:00-20:00\n篮球基础\n10/16", PRIMARY),
        (1, 1, "15:30-16:30\n羽毛球团课\n12/12", WARNING),
        (1, 5, "10:00-11:00\n跳绳专项\n7/10", PRIMARY),
        (2, 3, "18:30-19:30\n中考体育\n15/18", SUCCESS),
    ]
    for row, col, text, color in cards:
        x = 252 + col * 176
        y = 462 + row * 116
        draw.rounded_rectangle((x, y, x + 142, y + 72), radius=16, fill="#f7fbff", outline=color, width=3)
        lines = text.split("\n")
        for idx, line in enumerate(lines):
            draw.text((x + 14, y + 10 + idx * 18), line, font=FONT_TINY, fill=TEXT if idx < 2 else SUB)
    draw_paragraph(draw, 94, 808, "看板按周汇总教练排课，运营人员可以快速查看场次时间、地点、人数与状态，并从看板跳转至排课管理页进行新增、编辑、成团判断和结课操作。", 1360)
    save(img, "fig4-6-schedule-board.png")


def figure_checkin():
    img, draw = base_canvas("销课管理", "私教消课与团课结课共同支撑课程履约闭环")
    draw_card(draw, (56, 146, 560, 842), "私教课销课")
    steps = [
        ("① 选择学员", "输入手机号或昵称搜索学员"),
        ("② 选择课程", "按课包筛选可用私教课程"),
        ("③ 选择教练", "选择本次授课教练"),
        ("④ 选择上课时间", "仅允许录入当前及历史时间"),
        ("⑤ 销课备注", "记录上课内容、地点与补充说明"),
    ]
    y = 226
    for title, desc in steps:
        draw.text((88, y), title, font=FONT_SMALL, fill=TEXT)
        draw.rounded_rectangle((88, y + 30, 528, y + 78), radius=14, fill="#fbfcff", outline=LINE, width=2)
        draw.text((104, y + 45), desc, font=FONT_TINY, fill=SUB)
        y += 104
    draw_button(draw, (346, 770, 528, 820), "确认销课", primary=True)

    draw_card(draw, (596, 146, 1528, 488), "消课记录")
    headers = ["学员", "课程", "教练", "上课时间", "教练金额", "状态"]
    col_widths = [120, 150, 110, 220, 140, 120]
    rows = [
        ["张同学", "少儿篮球私教", "王教练", "2026-04-25 19:00", "¥149.50", "出勤"],
        ["李同学", "中考体育冲刺", "陈教练", "2026-04-25 20:00", "¥199.50", "出勤"],
        ["周同学", "少儿篮球私教", "王教练", "2026-04-24 18:30", "¥149.50", "缺勤"],
    ]
    draw_table(draw, 626, 228, 864, headers, rows, col_widths)

    draw_card(draw, (596, 524, 1528, 842), "团课结课")
    draw.text((626, 602), "系统按场次加载已报名学员，勾选未到场学员后统一生成签到记录，并同步完成订单收口。", font=FONT_SMALL, fill=SUB)
    headers2 = ["手机号", "昵称", "出勤标记", "操作"]
    col_widths2 = [210, 180, 160, 220]
    rows2 = [
        ["138****0021", "乐乐", "正常出勤", "参与本次结课"],
        ["139****1178", "沐沐", "正常出勤", "参与本次结课"],
        ["137****0845", "晨晨", "缺勤", "回滚场次状态"],
    ]
    draw_table(draw, 626, 646, 770, headers2, rows2, col_widths2)
    draw_button(draw, (1288, 770, 1498, 820), "确认结课", primary=True)
    save(img, "fig4-7-checkin-management.png")


def figure_coupon_income():
    img, draw = base_canvas("优惠券与收入统计", "围绕课程营销、订单优惠与收入口径进行统一管理")
    draw_card(draw, (56, 146, 840, 842), "优惠券管理")
    draw_button(draw, (636, 194, 808, 246), "新增优惠券", primary=True)
    headers = ["名称", "类型", "优惠内容", "适用范围", "发放方式", "状态"]
    col_widths = [140, 90, 150, 130, 140, 100]
    rows = [
        ["新用户课程券", "满减", "满199减30", "课程优惠券", "注册赠送", "启用"],
        ["团课周末券", "折扣", "满99打8折", "通用优惠券", "手动发放", "启用"],
        ["暑期拉新券", "无门槛", "立减20", "课程优惠券", "满额发放", "禁用"],
    ]
    draw_table(draw, 84, 280, 728, headers, rows, col_widths)
    draw_input(draw, (84, 544, 310, 590), "优惠券名称", "新用户课程券")
    draw_input(draw, (330, 544, 520, 590), "类型", "满减")
    draw_input(draw, (540, 544, 808, 590), "适用范围", "课程优惠券 / 通用优惠券")
    draw_paragraph(draw, 86, 628, "当前答辩版本隐藏了与商品相关的展示口径，页面仅保留通用优惠券和课程优惠券两类范围，保证前端配置与后端能力保持一致。", 706)

    draw_card(draw, (876, 146, 1528, 494), "收入统计")
    draw.rounded_rectangle((908, 220, 1180, 314), radius=20, fill="#eef4ff")
    draw.rounded_rectangle((1210, 220, 1496, 314), radius=20, fill="#fff3e7")
    draw.text((934, 242), "课程订单总收入", font=FONT_SMALL, fill=SUB)
    draw.text((934, 274), "¥ 58,240.00", font=FONT_H2, fill=PRIMARY)
    draw.text((1236, 242), "课程累计收入", font=FONT_SMALL, fill=SUB)
    draw.text((1236, 274), "¥ 58,240.00", font=FONT_H2, fill=WARNING)
    headers2 = ["订单号", "实付金额", "优惠金额", "状态", "支付时间"]
    col_widths2 = [190, 120, 120, 100, 180]
    rows2 = [
        ["KC20260425001", "¥299.00", "¥30.00", "已完成", "2026-04-25 15:30"],
        ["KC20260425008", "¥99.00", "¥0.00", "待排课", "2026-04-25 18:40"],
        ["KC20260426003", "¥399.00", "¥20.00", "已完成", "2026-04-26 09:12"],
    ]
    draw_table(draw, 908, 344, 588, headers2, rows2, col_widths2)
    draw_card(draw, (876, 526, 1528, 842), "教练结算")
    draw_input(draw, (908, 602, 1088, 648), "教练ID", "12")
    draw_input(draw, (1106, 602, 1288, 648), "开始日期", "2026-04-01")
    draw_input(draw, (1306, 602, 1496, 648), "结束日期", "2026-04-30")
    draw_button(draw, (1336, 674, 1496, 722), "发起结算", primary=True)
    draw_paragraph(draw, 908, 752, "教练结算模块先汇总未结算且已出勤的签到记录，再生成结算单并支持后台确认，确保收入统计、课酬计算与签到数据来源保持一致。", 588)
    save(img, "fig4-8-coupon-income-settlement.png")


def figure_ai_session():
    img, draw = base_canvas("AI 会话管理", "左侧会话列表 + 右侧消息工作台，支持人工接管和会话收口")
    draw_card(draw, (56, 146, 472, 842), "客服会话")
    sessions = [
        ("张同学", "想问课包还剩几节", "待人工", True),
        ("李家长", "周末团课什么时候上", "AI处理中", False),
        ("游客", "退款怎么申请", "已解决", False),
        ("王家长", "有没有适合三年级的课程", "AI处理中", True),
    ]
    sy = 226
    for name, preview, status, unread in sessions:
        draw.rounded_rectangle((82, sy, 446, sy + 116), radius=18, fill="#fbfcff", outline=LINE, width=2)
        draw.ellipse((104, sy + 24, 152, sy + 72), fill="#d8e6ff")
        draw.text((112, sy + 34), name[:1], font=FONT_H2, fill=PRIMARY)
        draw.text((172, sy + 26), name, font=FONT_SMALL, fill=TEXT)
        draw.text((172, sy + 56), preview, font=FONT_TINY, fill=SUB)
        tag_fill = WARNING if status == "待人工" else SUCCESS if status == "已解决" else PRIMARY
        draw_tag(draw, (332, sy + 24, 430, sy + 54), status, tag_fill)
        if unread:
            draw.rounded_rectangle((406, sy + 70, 434, sy + 98), radius=14, fill=DANGER)
            draw.text((415, sy + 76), "2", font=FONT_TINY, fill="white")
        sy += 136

    draw_card(draw, (508, 146, 1528, 842), "会话工作台")
    draw.text((540, 214), "当前用户：张同学 · 138****0021 · 人工处理中", font=FONT_SMALL, fill=SUB)
    bubbles = [
        ("user", "我的篮球私教课包还剩几节？"),
        ("assistant", "已为您查询到当前课包总课时 12 节，已使用 7 节，剩余 5 节。"),
        ("user", "那最近一次销课是什么时候？"),
        ("assistant", "最近一次销课时间为 2026-04-25 19:00，由王教练完成记录。"),
    ]
    by = 266
    for role, content in bubbles:
        left = 566 if role == "assistant" else 810
        right = 1412 if role == "assistant" else 1470
        fill = "#f1f6ff" if role == "assistant" else "#e8fff1"
        draw.rounded_rectangle((left, by, right, by + 68), radius=18, fill=fill, outline=LINE, width=2)
        draw_paragraph(draw, left + 18, by + 16, content, right - left - 36, font=FONT_TINY, fill=TEXT, line_gap=4)
        by += 92
    draw.rounded_rectangle((548, 620, 1488, 714), radius=18, fill="#fbfcff", outline=LINE, width=2)
    draw.text((570, 646), "输入人工回复内容，用户在小程序端会实时看到", font=FONT_SMALL, fill="#9aa8b5")
    draw_button(draw, (1208, 738, 1316, 786), "标记解决")
    draw_button(draw, (1332, 738, 1488, 786), "发送回复", primary=True)
    draw_paragraph(draw, 550, 804, "工作台支持历史轮次切换、快捷回复、转人工记录和用户反馈展示，便于客服人员完整了解上下文并进行人工兜底。", 922)
    save(img, "fig4-9-ai-session.png")


def figure_ai_knowledge():
    img, draw = base_canvas("AI 知识库管理", "维护标题、分类、关键词、优先级与标准回复内容")
    draw_card(draw, (56, 146, 980, 842), "知识库列表")
    draw_input(draw, (84, 220, 304, 266), "搜索", "搜索标题 / 关键词 / 内容")
    draw_input(draw, (326, 220, 486, 266), "分类", "course")
    draw_input(draw, (506, 220, 646, 266), "状态", "启用")
    draw_button(draw, (816, 214, 948, 266), "新增知识", primary=True)
    headers = ["标题", "分类", "关键词", "优先级", "状态", "内容摘要"]
    col_widths = [180, 100, 220, 90, 90, 180]
    rows = [
        ["课包剩余节数查询", "course", "课包,剩余,节数", "20", "启用", "查询用户课包并返回剩余课时"],
        ["退款流程说明", "refund", "退款,取消,驳回", "18", "启用", "说明退款路径与审核规则"],
        ["优惠券使用规则", "coupon", "优惠券,折扣,满减", "12", "启用", "回答适用范围与生效条件"],
    ]
    draw_table(draw, 84, 302, 868, headers, rows, col_widths)
    draw_paragraph(draw, 84, 584, "知识库命中后，系统会优先使用已配置的标准内容回复高频问题；若匹配结果不充分，再交给模型进行增强润色。", 850)

    draw_card(draw, (1016, 146, 1528, 842), "新增 / 编辑知识")
    draw_input(draw, (1048, 232, 1498, 278), "标题", "课包剩余节数查询")
    draw_input(draw, (1048, 318, 1248, 364), "分类", "course")
    draw_input(draw, (1268, 318, 1498, 364), "优先级", "20")
    draw_input(draw, (1048, 404, 1498, 450), "关键词", "课包, 剩余, 节数, 次数")
    draw_input(draw, (1048, 490, 1498, 536), "状态", "启用")
    draw.text((1048, 574), "内容", font=FONT_SMALL, fill=SUB)
    draw.rounded_rectangle((1048, 604, 1498, 760), radius=18, fill="#fbfcff", outline=LINE, width=2)
    draw_paragraph(draw, 1066, 622, "您好，已为您查询到当前课包总课时 12 节，已使用 7 节，剩余 5 节。若您还需要查看最近一次销课记录，也可以继续告诉我。", 414)
    draw_button(draw, (1244, 782, 1358, 826), "取消")
    draw_button(draw, (1372, 782, 1498, 826), "保存", primary=True)
    save(img, "fig4-10-ai-knowledge.png")


def main():
    figure_course()
    figure_schedule()
    figure_checkin()
    figure_coupon_income()
    figure_ai_session()
    figure_ai_knowledge()
    print(ASSET_DIR)


if __name__ == "__main__":
    main()
