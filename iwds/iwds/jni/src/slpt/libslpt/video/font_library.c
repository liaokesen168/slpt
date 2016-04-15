/*
 * Copyright (C) 2014 Ingenic Semiconductor Co., Ltd.
 * Authors: Kage Shen <kkshen@ingenic.cn>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General  Public License as published by the
 * Free Software Foundation;  either version 2 of the License, or (at your
 * option) any later version.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */


unsigned char num_font_64_32[][256] = {
		{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0F,0xE0,0x00,0x00,0x3F,0xF8,0x00,
		0x00,0xFF,0xFE,0x00,0x01,0xFF,0xFF,0x00,0x01,0xF8,0x3F,0x00,0x03,0xF0,0x1F,0x80,
		0x03,0xE0,0x0F,0x80,0x07,0xC0,0x07,0xC0,0x07,0xC0,0x07,0xC0,0x0F,0x80,0x03,0xE0,
		0x0F,0x80,0x03,0xE0,0x0F,0x80,0x03,0xE0,0x0F,0x80,0x03,0xE0,0x0F,0x00,0x01,0xE0,
		0x1F,0x00,0x01,0xF0,0x1F,0x00,0x01,0xF0,0x1F,0x00,0x01,0xF0,0x1F,0x00,0x01,0xF0,
		0x1F,0x03,0x81,0xF0,0x1F,0x07,0xC1,0xF0,0x1F,0x0F,0xE1,0xF0,0x1F,0x0F,0xE1,0xF0,
		0x1F,0x0F,0xE1,0xF0,0x1F,0x07,0xC1,0xF0,0x1F,0x03,0x81,0xF0,0x1F,0x00,0x01,0xF0,
		0x1F,0x00,0x01,0xF0,0x1F,0x00,0x01,0xF0,0x0F,0x00,0x01,0xE0,0x0F,0x80,0x03,0xE0,
		0x0F,0x80,0x03,0xE0,0x0F,0x80,0x03,0xE0,0x0F,0x80,0x03,0xE0,0x07,0xC0,0x07,0xC0,
		0x07,0xC0,0x07,0xC0,0x03,0xE0,0x0F,0x80,0x03,0xF0,0x1F,0x80,0x01,0xF8,0x3F,0x00,
		0x01,0xFF,0xFF,0x00,0x00,0xFF,0xFE,0x00,0x00,0x3F,0xF8,0x00,0x00,0x0F,0xE0,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"0",0*/
		},{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0xF0,0x00,
		0x00,0xFF,0xF0,0x00,0x03,0xFF,0xF0,0x00,0x03,0xFF,0xF0,0x00,0x03,0xF9,0xF0,0x00,
		0x03,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,
		0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,
		0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,
		0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,
		0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,
		0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,
		0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,
		0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x01,0xFF,0xFF,0xF0,
		0x01,0xFF,0xFF,0xF0,0x01,0xFF,0xFF,0xF0,0x01,0xFF,0xFF,0xF0,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"1",1*/
		},{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x1F,0xF0,0x00,0x01,0xFF,0xFE,0x00,
		0x07,0xFF,0xFF,0x00,0x0F,0xFF,0xFF,0xC0,0x0F,0xF0,0x1F,0xE0,0x0F,0x80,0x07,0xE0,
		0x0E,0x00,0x03,0xF0,0x08,0x00,0x01,0xF0,0x00,0x00,0x00,0xF8,0x00,0x00,0x00,0xF8,
		0x00,0x00,0x00,0xF8,0x00,0x00,0x00,0xF8,0x00,0x00,0x00,0xF8,0x00,0x00,0x00,0xF8,
		0x00,0x00,0x00,0xF8,0x00,0x00,0x01,0xF8,0x00,0x00,0x01,0xF8,0x00,0x00,0x03,0xF0,
		0x00,0x00,0x07,0xF0,0x00,0x00,0x07,0xE0,0x00,0x00,0x0F,0xC0,0x00,0x00,0x1F,0xC0,
		0x00,0x00,0x3F,0x80,0x00,0x00,0x7F,0x00,0x00,0x00,0xFE,0x00,0x00,0x01,0xFC,0x00,
		0x00,0x03,0xF8,0x00,0x00,0x07,0xF0,0x00,0x00,0x0F,0xE0,0x00,0x00,0x1F,0xC0,0x00,
		0x00,0x3F,0x80,0x00,0x00,0x7F,0x00,0x00,0x00,0xFE,0x00,0x00,0x01,0xFC,0x00,0x00,
		0x03,0xF8,0x00,0x00,0x07,0xF0,0x00,0x00,0x0F,0xE0,0x00,0x00,0x0F,0xFF,0xFF,0xF8,
		0x0F,0xFF,0xFF,0xF8,0x0F,0xFF,0xFF,0xF8,0x0F,0xFF,0xFF,0xF8,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"2",2*/
		},{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x3F,0xE0,0x00,0x03,0xFF,0xF8,0x00,
		0x0F,0xFF,0xFE,0x00,0x0F,0xFF,0xFF,0x00,0x0F,0xC0,0x3F,0x80,0x0E,0x00,0x0F,0xC0,
		0x08,0x00,0x07,0xC0,0x00,0x00,0x07,0xE0,0x00,0x00,0x03,0xE0,0x00,0x00,0x03,0xE0,
		0x00,0x00,0x03,0xE0,0x00,0x00,0x03,0xE0,0x00,0x00,0x03,0xE0,0x00,0x00,0x07,0xE0,
		0x00,0x00,0x07,0xC0,0x00,0x00,0x0F,0xC0,0x00,0x00,0x3F,0x80,0x00,0x3F,0xFF,0x00,
		0x00,0x3F,0xFC,0x00,0x00,0x3F,0xFC,0x00,0x00,0x3F,0xFF,0x00,0x00,0x00,0x3F,0x80,
		0x00,0x00,0x0F,0xC0,0x00,0x00,0x07,0xC0,0x00,0x00,0x03,0xE0,0x00,0x00,0x03,0xE0,
		0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,
		0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x03,0xF0,
		0x00,0x00,0x03,0xE0,0x10,0x00,0x07,0xE0,0x1C,0x00,0x0F,0xC0,0x1F,0x80,0x3F,0xC0,
		0x1F,0xFF,0xFF,0x80,0x1F,0xFF,0xFE,0x00,0x07,0xFF,0xFC,0x00,0x00,0x7F,0xE0,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"3",3*/
		},{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x1F,0x80,
		0x00,0x00,0x3F,0x80,0x00,0x00,0x7F,0x80,0x00,0x00,0x7F,0x80,0x00,0x00,0xFF,0x80,
		0x00,0x01,0xEF,0x80,0x00,0x01,0xEF,0x80,0x00,0x03,0xCF,0x80,0x00,0x07,0xCF,0x80,
		0x00,0x07,0x8F,0x80,0x00,0x0F,0x0F,0x80,0x00,0x0F,0x0F,0x80,0x00,0x1E,0x0F,0x80,
		0x00,0x3E,0x0F,0x80,0x00,0x3C,0x0F,0x80,0x00,0x78,0x0F,0x80,0x00,0xF8,0x0F,0x80,
		0x00,0xF0,0x0F,0x80,0x01,0xE0,0x0F,0x80,0x03,0xE0,0x0F,0x80,0x03,0xC0,0x0F,0x80,
		0x07,0xC0,0x0F,0x80,0x07,0x80,0x0F,0x80,0x0F,0x00,0x0F,0x80,0x1F,0x00,0x0F,0x80,
		0x1E,0x00,0x0F,0x80,0x1F,0xFF,0xFF,0xFC,0x1F,0xFF,0xFF,0xFC,0x1F,0xFF,0xFF,0xFC,
		0x1F,0xFF,0xFF,0xFC,0x00,0x00,0x0F,0x80,0x00,0x00,0x0F,0x80,0x00,0x00,0x0F,0x80,
		0x00,0x00,0x0F,0x80,0x00,0x00,0x0F,0x80,0x00,0x00,0x0F,0x80,0x00,0x00,0x0F,0x80,
		0x00,0x00,0x0F,0x80,0x00,0x00,0x0F,0x80,0x00,0x00,0x0F,0x80,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"4",4*/
		},{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0xFF,0xFF,0x80,
		0x07,0xFF,0xFF,0x80,0x07,0xFF,0xFF,0x80,0x07,0xFF,0xFF,0x80,0x07,0xC0,0x00,0x00,
		0x07,0xC0,0x00,0x00,0x07,0xC0,0x00,0x00,0x07,0xC0,0x00,0x00,0x07,0xC0,0x00,0x00,
		0x07,0xC0,0x00,0x00,0x07,0xC0,0x00,0x00,0x07,0xC0,0x00,0x00,0x07,0xC0,0x00,0x00,
		0x07,0xDF,0xE0,0x00,0x07,0xFF,0xF8,0x00,0x07,0xFF,0xFE,0x00,0x07,0xFF,0xFF,0x00,
		0x07,0x80,0x7F,0x80,0x04,0x00,0x1F,0xC0,0x00,0x00,0x0F,0xC0,0x00,0x00,0x07,0xE0,
		0x00,0x00,0x03,0xE0,0x00,0x00,0x03,0xE0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,
		0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,
		0x00,0x00,0x01,0xF0,0x00,0x00,0x01,0xF0,0x00,0x00,0x03,0xE0,0x00,0x00,0x03,0xE0,
		0x10,0x00,0x07,0xE0,0x18,0x00,0x0F,0xC0,0x1E,0x00,0x1F,0xC0,0x1F,0xC0,0x7F,0x80,
		0x1F,0xFF,0xFF,0x00,0x1F,0xFF,0xFE,0x00,0x0F,0xFF,0xF8,0x00,0x00,0xFF,0xC0,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"5",5*/
		},{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x03,0xF8,0x00,0x00,0x1F,0xFF,0x00,
		0x00,0x7F,0xFF,0x80,0x00,0xFF,0xFF,0x80,0x01,0xFE,0x07,0x80,0x01,0xF8,0x01,0x80,
		0x03,0xF0,0x00,0x80,0x07,0xE0,0x00,0x00,0x07,0xC0,0x00,0x00,0x07,0xC0,0x00,0x00,
		0x0F,0x80,0x00,0x00,0x0F,0x80,0x00,0x00,0x0F,0x80,0x00,0x00,0x0F,0x00,0x00,0x00,
		0x1F,0x07,0xF0,0x00,0x1F,0x1F,0xFC,0x00,0x1F,0x3F,0xFF,0x00,0x1F,0x7F,0xFF,0x80,
		0x1F,0xF8,0x1F,0xC0,0x1F,0xF0,0x0F,0xC0,0x1F,0xE0,0x07,0xE0,0x1F,0xC0,0x03,0xE0,
		0x1F,0xC0,0x03,0xE0,0x1F,0xC0,0x01,0xF0,0x1F,0x80,0x01,0xF0,0x1F,0x80,0x01,0xF0,
		0x1F,0x80,0x01,0xF0,0x1F,0x80,0x01,0xF0,0x1F,0x80,0x01,0xF0,0x0F,0x80,0x01,0xF0,
		0x0F,0x80,0x01,0xF0,0x0F,0x80,0x01,0xF0,0x0F,0xC0,0x01,0xF0,0x07,0xC0,0x03,0xE0,
		0x07,0xC0,0x03,0xE0,0x07,0xE0,0x07,0xE0,0x03,0xF0,0x0F,0xC0,0x03,0xF8,0x1F,0x80,
		0x01,0xFF,0xFF,0x80,0x00,0xFF,0xFF,0x00,0x00,0x3F,0xFC,0x00,0x00,0x0F,0xF0,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"6",6*/
		},{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x1F,0xFF,0xFF,0xF0,
		0x1F,0xFF,0xFF,0xF0,0x1F,0xFF,0xFF,0xF0,0x1F,0xFF,0xFF,0xF0,0x00,0x00,0x03,0xE0,
		0x00,0x00,0x07,0xE0,0x00,0x00,0x07,0xC0,0x00,0x00,0x07,0xC0,0x00,0x00,0x0F,0xC0,
		0x00,0x00,0x0F,0x80,0x00,0x00,0x1F,0x80,0x00,0x00,0x1F,0x00,0x00,0x00,0x1F,0x00,
		0x00,0x00,0x3F,0x00,0x00,0x00,0x3E,0x00,0x00,0x00,0x3E,0x00,0x00,0x00,0x7C,0x00,
		0x00,0x00,0x7C,0x00,0x00,0x00,0xFC,0x00,0x00,0x00,0xF8,0x00,0x00,0x00,0xF8,0x00,
		0x00,0x01,0xF8,0x00,0x00,0x01,0xF0,0x00,0x00,0x03,0xF0,0x00,0x00,0x03,0xE0,0x00,
		0x00,0x03,0xE0,0x00,0x00,0x07,0xE0,0x00,0x00,0x07,0xC0,0x00,0x00,0x0F,0xC0,0x00,
		0x00,0x0F,0x80,0x00,0x00,0x0F,0x80,0x00,0x00,0x1F,0x80,0x00,0x00,0x1F,0x00,0x00,
		0x00,0x1F,0x00,0x00,0x00,0x3F,0x00,0x00,0x00,0x3E,0x00,0x00,0x00,0x7E,0x00,0x00,
		0x00,0x7C,0x00,0x00,0x00,0x7C,0x00,0x00,0x00,0xFC,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"7",7*/
		},{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x1F,0xF0,0x00,0x00,0x7F,0xFC,0x00,
		0x00,0xFF,0xFF,0x00,0x03,0xFF,0xFF,0x80,0x03,0xF8,0x3F,0x80,0x07,0xE0,0x0F,0xC0,
		0x07,0xC0,0x07,0xC0,0x0F,0xC0,0x07,0xE0,0x0F,0x80,0x03,0xE0,0x0F,0x80,0x03,0xE0,
		0x0F,0x80,0x03,0xE0,0x0F,0x80,0x03,0xE0,0x0F,0x80,0x03,0xE0,0x0F,0x80,0x03,0xE0,
		0x07,0xC0,0x07,0xC0,0x07,0xC0,0x07,0xC0,0x03,0xE0,0x0F,0x80,0x01,0xF8,0x3F,0x00,
		0x00,0xFF,0xFE,0x00,0x00,0x3F,0xF8,0x00,0x00,0x7F,0xFC,0x00,0x01,0xFF,0xFF,0x00,
		0x03,0xF0,0x1F,0x80,0x07,0xE0,0x0F,0xC0,0x0F,0xC0,0x07,0xE0,0x0F,0x80,0x03,0xE0,
		0x0F,0x80,0x03,0xE0,0x1F,0x00,0x01,0xF0,0x1F,0x00,0x01,0xF0,0x1F,0x00,0x01,0xF0,
		0x1F,0x00,0x01,0xF0,0x1F,0x00,0x01,0xF0,0x1F,0x00,0x01,0xF0,0x1F,0x80,0x03,0xF0,
		0x0F,0x80,0x03,0xE0,0x0F,0xC0,0x07,0xE0,0x0F,0xE0,0x0F,0xE0,0x07,0xF0,0x1F,0xC0,
		0x03,0xFF,0xFF,0x80,0x01,0xFF,0xFF,0x00,0x00,0x7F,0xFC,0x00,0x00,0x1F,0xF0,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"8",8*/
		},{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x1F,0xE0,0x00,0x00,0x7F,0xF8,0x00,
		0x01,0xFF,0xFE,0x00,0x03,0xFF,0xFF,0x00,0x07,0xF0,0x3F,0x80,0x07,0xE0,0x1F,0x80,
		0x0F,0xC0,0x0F,0xC0,0x0F,0x80,0x07,0xC0,0x0F,0x80,0x07,0xC0,0x1F,0x00,0x07,0xE0,
		0x1F,0x00,0x03,0xE0,0x1F,0x00,0x03,0xE0,0x1F,0x00,0x03,0xE0,0x1F,0x00,0x03,0xF0,
		0x1F,0x00,0x03,0xF0,0x1F,0x00,0x03,0xF0,0x1F,0x00,0x03,0xF0,0x1F,0x00,0x03,0xF0,
		0x1F,0x00,0x07,0xF0,0x0F,0x80,0x07,0xF0,0x0F,0x80,0x07,0xF0,0x0F,0xC0,0x0F,0xF0,
		0x07,0xE0,0x1F,0xF0,0x07,0xF0,0x3F,0xF0,0x03,0xFF,0xFD,0xF0,0x01,0xFF,0xF9,0xF0,
		0x00,0x7F,0xF1,0xF0,0x00,0x1F,0xC1,0xF0,0x00,0x00,0x01,0xE0,0x00,0x00,0x03,0xE0,
		0x00,0x00,0x03,0xE0,0x00,0x00,0x03,0xE0,0x00,0x00,0x07,0xC0,0x00,0x00,0x07,0xC0,
		0x00,0x00,0x0F,0x80,0x02,0x00,0x1F,0x80,0x03,0x00,0x3F,0x00,0x03,0xC0,0xFF,0x00,
		0x03,0xFF,0xFE,0x00,0x03,0xFF,0xFC,0x00,0x01,0xFF,0xF0,0x00,0x00,0x3F,0x80,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"9",9*/
		}

};

unsigned char maoh[2][256] = {
		{

				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0xF0,0x00,
				0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,
				0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0xF0,0x00,
				0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,
				0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,0x00,0x07,0xF0,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*":",0*/

		},
		{
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		}
};

unsigned char num_font_32_16[][64] = {
		{     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x03,0xE0,0x0F,0xF8,
              0x0E,0x38,0x1C,0x1C,0x1C,0x1C,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x39,0xCE,0x39,0xCE,
              0x39,0xCE,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x1C,0x1C,0x1C,0x1C,0x0E,0x38,
              0x0F,0xF8,0x03,0xE0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"0",0*/

		},{   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x03,0xE0,0x0F,0xE0,
              0x0C,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,
              0x00,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,0x00,0xE0,
              0x0F,0xFE,0x0F,0xFE,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"1",1*/

		},{   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0F,0xE0,0x3F,0xF8,
              0x38,0x3C,0x20,0x1E,0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x1E,0x00,0x3C,
              0x00,0x7C,0x00,0xF8,0x00,0xF0,0x01,0xE0,0x03,0xC0,0x07,0x00,0x0E,0x00,0x1C,0x00,
              0x3F,0xFE,0x3F,0xFE,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"2",2*/

		},{   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0xF0,0x1F,0xF8,
              0x18,0x1C,0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x3C,0x07,0xF0,0x07,0xF0,
              0x00,0x3C,0x00,0x1C,0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x1E,0x30,0x3C,
              0x3F,0xF8,0x0F,0xE0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"3",3*/

		},{   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x70,0x00,0xF0,
              0x01,0xF0,0x01,0xF0,0x03,0x70,0x07,0x70,0x06,0x70,0x0C,0x70,0x0C,0x70,0x18,0x70,
              0x38,0x70,0x30,0x70,0x60,0x70,0x7F,0xFE,0x7F,0xFE,0x00,0x70,0x00,0x70,0x00,0x70,
              0x00,0x70,0x00,0x70,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"4",4*/

		},{   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x1F,0xFC,0x1F,0xFC,
              0x1C,0x00,0x1C,0x00,0x1C,0x00,0x1C,0x00,0x1F,0xE0,0x1F,0xF8,0x10,0x3C,0x00,0x1C,
              0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x0E,0x00,0x1C,0x20,0x3C,
              0x3F,0xF8,0x1F,0xE0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"5",5*/

		},{   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01,0xF8,0x07,0xFC,
              0x0F,0x04,0x1E,0x00,0x1C,0x00,0x1C,0x00,0x38,0x00,0x39,0xF0,0x3B,0xF8,0x3E,0x3C,
              0x3C,0x1E,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x18,0x0E,0x1C,0x1C,0x0E,0x3C,
              0x0F,0xF8,0x03,0xF0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"6",6*/

		},{   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x3F,0xFE,0x3F,0xFE,
              0x00,0x1E,0x00,0x1C,0x00,0x1C,0x00,0x38,0x00,0x38,0x00,0x38,0x00,0x70,0x00,0x70,
              0x00,0xF0,0x00,0xE0,0x00,0xE0,0x01,0xE0,0x01,0xC0,0x01,0xC0,0x03,0x80,0x03,0x80,
              0x03,0x80,0x07,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"7",7*/

		},{   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0xF0,0x1F,0xFC,
              0x1C,0x1C,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x1C,0x1C,0x07,0xF0,0x0F,0xF8,
              0x1E,0x3C,0x1C,0x1C,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x3C,0x1E,0x1E,0x3C,
              0x0F,0xF8,0x07,0xF0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"8",8*/

		},{   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0xE0,0x0F,0xF8,
              0x1E,0x38,0x3C,0x1C,0x38,0x0C,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x38,0x0E,0x3C,0x1E,
              0x1E,0x3E,0x0F,0xEE,0x07,0xCE,0x00,0x0E,0x00,0x1C,0x00,0x1C,0x00,0x3C,0x10,0x78,
              0x1F,0xF0,0x0F,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,/*"9",9*/
		}
};