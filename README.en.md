Modified AozoraEpub3
============
description of the modified version
------------
This is a fork version that aims to be close to the EPUB of the JASRAC. When using Aozora Epub 3 for electronic publishing purposes, the original version may not pass the review. Through EPUB validation, we ensure that many EPUB viewers have no display problems.

Due to a Java licensing issue, we decided to build with the AdoptOpenJDK. https://adoptopenjdk.net/releases.html OpenJDK 11 (LTS), HotSpot, OS, and Install JRE.


Downloads
============
Check the releases page [releases page](https://github.com/kyukyunyorituryo/AozoraEpub3/releases) to get the latest distribution.

Description
------------
This is a tool to convert text files with notes from Aozora Bunko into ePub 3 files. ・ Convert the text and image file (or zip) of Aozora Bunko txt to ePub 3 ・ Get the HTML of the Web novel and save it in Aozora Bunko txt format then convert it to ePub 3 ・ Convert the image zip/rar to ePub 3.

Usage Notes
------------
Please use it at your own risk.

* There are some notes that are currently not supported.
* Notes from Aozora Bunko: Some xhtml errors converted by non-specification notes may not be displayed.
* When a 4-byte character is output, it may not be displayed after the external character on an incompatible terminal. (Show notes as small when option not converted is selected)
Please report it on the distribution site where there are bugs or notes that cannot be converted.

Notes on Conversion
------------
Abnormalities in comments, notes that are not supported, and private characters that could not be converted are displayed in the log at the time of conversion, so correct the original text accordingly.

- Out-of-specification and some fluctuating notes are not supported.
- If a Gaiji Note is used in a Gaiji Note, an error will be generated (No plans to respond) → ※［＃「姉」の正字、「女＋※［＃第3水準1-85-57］のつくり」 is shown in the log, the original text of that part is corrected to ※［＃「姉」の正字、U+59CA］.
- Please delete original comment notes with notes in them

System Requirements
------------
Java 8 and later system requirements (http://www.java.com/ja/)
AdoptOpenJDK (https://adoptopenjdk.net/releases.html)

Windows XP or later works with Ubuntu Mac OS X.


How to use
------------
#### Installation
Unzip AozoraEpub3 -*. zip to any folder.

#### Start
Double-click AozoraEpub3.jar to run it.
or "java -jar AozoraEpub3.jar" from the console.
* If java is not visible, specify the full path.
Example: "C: \Program Files \AdoptOpenJDK \jre- 11.0.5.10 -hotspot \bin/java.exe" -jar AozoraEpub3.jar

#### EPUB convert
Aozora Bunko text file (Extension txt or zip) to be converted into the displayed applet
Drag and drop (Multiple). (Same as opening from "File Selection")
The "Original Filename .epub" or "[author's name] Title. epub" file is generated in the same location as the text file.
* If you convert the image only zip without text, it will generate an ePub file with only images.

#### Converting Web novels
You can also use drag-and-drop to retrieve and convert URLs or URL shortcuts (.url) on the list page of a Web novel site. (Only sites with definition files in web /)

You can obtain it from "syosetu.com", (+ Related Sites), "NEWVEL-LIBRARY", "FC2 Novels", "HAMELN", "Arcadia", "novelist.jp", "dNoVeLs", "Kakyomu", and "novelup.plus".
https://syosetu.com , https://novel.fc2.com/ , https://syosetu.org/ , http://www.mai-net.net/ , http://novelist.jp/ , http://www.dnovels.net/ , https://kakuyomu.jp/ , https://novelup.plus/


screen setting
------------
#### Title
* In the text
Sets whether the title and author name are included in the text.
If it is three lines in a row, the title is followed by a subtitle, which is concatenated with the title.
The title in the text is set to large characters with the author's name and title.
Images and blank lines are ignored.
Select "First Publisher" to treat line 1 as the publisher
* Filename Override
Gets the title and author name from the "[author's name] Title. epub" file name.
The style settings for the title and author lines in the body of the text follow the selections in the body of the text.

#### Cover
* front cover
Specify the cover page image as [leading illustration] [Same image as input file name (png, jpg)] [No Cover] or a file or URL.
[Same image as input file name (png, jpg)] uses an image with the same name and extension as the cover page.
(The extensions are checked in the following order: png, jpg, jpeg, gif)
If there is a cover.png | jpg | jpeg file in the path of the text file when there is no cover page image, set it as a cover page on the confirmation screen.

#### Page Output
* front cover
Add a cover page (Image is 100% width) to the beginning of the ePub.
Please specify it when you want to show the cover in Reader, etc.
(If you specify an illustration in the text as the cover page, it is moved to the top.)
* Title
Title Print the title, author, or other page as a single page, centered horizontally or horizontally.
* Table of Contents
Select to print a table of contents page.
You can choose vertical or horizontal writing.

* Extension
Specifies the extension of the output file.
Choose ".kepub.epub" for Kobo
".fxl.kepub.epub" is the extension for Kobo fixed layout
Select ".mobi" to convert epub to mobi at Kindlegen.exe
Select ".mobi + .epub" to output the unconverted epub file at the same time.
* Use title for output file name
"[author's name] Title. epub" file name.
If neither is set, "Original Filename .epub" is output.
* ePub file overwrite
If a file with the same name (Original Filename .epub) already exists, it is overwritten and output.
If unchecked, the same files will not be converted.

#### Destination
* Destination
Select "Same as Input" to output to the path of the input file.
Sets the full path when specifying the output destination in "path specification".

#### Conversion Setup
* Bookmark ID output
Set the p tag of the line to the id (kobo. 1.1 format) for the bookmark in Kobo's kepub.
It is not required in environments other than Kobo's kepub.
* 4-byte character conversion
If unchecked, 4-byte characters will be converted to = and the note will be displayed in small letters at the end.
(There is a problem with Kobo not displaying more than 4 byte characters in a line.)
In Reader, 4-byte JIS characters are displayed.
(However, those Kanji characters that cannot be displayed are displayed together with kanji characters and the annotation of small letters is not displayed.)
* Vertical Horizontal
Specifies vertical or horizontal text flow in the body.

#### Transform
* input character code
Specifies the character code of the Aozora Bunko file to be entered. This is usually MS 932 (SJIS).
* File Selection
Selecting a file converts it as if you were dragging and dropping it into the text area.
* Pre-conversion Check
Displays a dialog where you can review and edit the title, author, and cover page before converting.
Metadata is created with the modified title and author name.
The title or style of the message is not changed.
You can specify that the cover page is trimmed and the original image is retained.

----
#### Picture Setting 1
* illustration exclusion
Does not display illustration images in text and does not store them in ePub files. The cover page and external character image are output.
* screen size
Use to determine the screen aspect ratio and when not enlarging a small image
* Cover Size
The cover image is reduced to be smaller than this size.
* image magnification
Specifies a percentage of the width of the image, relative to the number of pixels in the image and the screen resolution.
*If the aspect ratio changes due to screen rotation, the image may protrude downward.
* image wrap
Arrange the image so that the characters wrap around the top and bottom of the image in the text.
Only images smaller than the specified image size are displayed.
* Image Single Page
Sets the size of the image to be made into a single page by inserting a page break before and after the image in the text
Output as a page displaying only images
* Thumbnails view
Images smaller than the screen size are enlarged to fit the height or width of the screen.
#### Picture Setting 2
* Jpeg Image Quality When Reduced
Jepg compression parameter for scaling 100 is highest quality
* image reduction rotation
Reduces the image to less than or equal to the number of pixels (The scaling algorithm is Bicubic.)
Set when terminal size is limited
You can also set it to rotate according to the aspect ratio of the image and the screen.
* margin removal
Removes the top, bottom, left, and right margins from the image.
Available only for image only zip/rar files
Png is slightly larger due to the input/output time and compression rate.
(Recommended settings Horizontal: 15% Vertical: 10 ~ 20% White Level: 85 ~ 90% Margins Additional: 0.5% to 1.0%)

----
#### Advanced Settings
* processing full-width spaces in a sentence
? If there is a double-byte space after, etc., it will appear like a paragraph at the beginning of a line after the second line, so hide the space.
* blank line removal
Reduces the number of blank lines specified in one or more consecutive blank lines.
Leave at least one blank line starting with the last three lines of the header row.
If a maximum is specified, it is removed so that it is less than or equal to the empty line.
* Indent
If the line starts with "-" (< [If not, add a double-byte space at the beginning of the line.
* Automatic Tate-chu-yoko
2 half-width numbers and 2 ~ 3! characters! What? tiled vertically.
One digit, three digits,!? You can change a single character to Tate-chu-yoko or Tate-chu-yoko in the settings.
It is disabled when there is no double-byte character before or after it (Ignore spaces between) or in a horizontal note.
* comment output
Specifies how a comment block separated by a - line of at least 50 characters is displayed.
* forced page break
When enabled, forces a page break at the specified number of bytes.
The increase in the size and number of lines of each xhtml file in ePub prevents heavy processing in Reader, etc.
The page break will not occur if it is in a block note such as indentation.
Each line: Force page breaks on lines that exceed the specified number of bytes.
Blank line: Force page break if the number of blank lines exceeds the specified number of bytes.
Before heading: Forces a page break if the specified number of bytes is exceeded before the corresponding row in the table of contents heading.

----
#### TOC Settings
* Table of Contents Output
Maximum Characters: Sets the maximum number of characters for the table of contents name. If long characters are omitted, ... is appended.
Cover page: Prints a table of contents to the cover page. If there is no cover image, it is not output.
Join next line: If the chapter title is on the next line, the character on the next line of the heading is joined to the name of the table of contents.
Suppress Consecutive Headings: Prevents headings that are automatically extracted from the table of contents page, etc., from being included in the table of contents.
* Table of Contents Extraction
After Page Break: Adds the first line of characters to the table of contents after the page break.
Note: Adds the text in the selected heading note to the table of contents. For block notes, only the following lines (Two lines when connected)
Chapter headings: Automatically extract chapter names (numeric) and add them to the table of contents.
(Chapter ~/Chapter ~/Part ~/Part ~ Part ~ Section ~ Section/Chapter ~/Part ~/Chapter ~/Chapter/Prologue/Epilogue/Monologue/Introduction/Final Chapter/Interchapter/Change Chapter/Intermission)
Number Only: Adds a line containing only numbers to the table of contents.
Number + Header: Adds a line of numbers + spaces + header characters to the table of contents.
Numbers (in parentheses): Adds lines containing only numbers in parentheses to the table of contents. [] [] ()
Add a line of numbers (in parentheses) + headings: (Numeric) + spaces, etc + headings to the table of contents.
Other Pattern: Specify the table of contents extraction pattern as a regular expression. Compares to a string without leading and trailing blanks and note tags.


#### Style Settings
* Row Height
The height of a line, in characters. 1.8 leaves 0.8 characters between lines.
* Text Size
Specifies a scale factor to adjust the standard text size.
* Text margins (@ page margin)
Specifies the top, bottom, left, and right margins of the page.
* Text margins (html margin)
Specifies the top, bottom, left, and right margins of the page.
I use this because @ page doesn't work in Reader.

* voiced/semi-voiced character
You can choose to output as is or stack with the position specification.
It is invalid in ruby.
Except for Reader, Kobo and Kindle, it is not confirmed to work.

Usage CUI
------------
#### Running from the Command Line
Usage: java -cp AozoraEpub3.jar AozoraEpub3 \[-options] input _ files (txt, zip)

** Options * *
--h, --help
show usage
--i, --ini <arg>
Imports settings from the specified ini file (Other than command line options)
(Default value if no AozoraEpub3.ini file is specified)

--enc <arg>
Input file encoding \[MS 932] (default) [UTF -8]
--t <arg>
Title kind in text \[0: Title - Author Name] (default) [1: Author Name - Title] [2: Title - Author Name (subtitle preference)] [3: Title Only] [4: None]
--c, --cover <arg>
Cover image \[0: First illustration] [1: Same image as file name] [File name or URL]
--tf
Use Input File Name as Title

--d, --dst <arg>
Destination Path
--ext <arg>
Output File Extension \[.epub] (default) [.kepub.epub]
--of <arg>
Match output file name to input file name

File Description
------------
#### Program Files
* AozoraEpub3.jar
ePub 3 conversion tool
Double click or "java -jar AozoraEpub3.jar"
* AozoraEpub 3.ico
Specify this icon when creating a shortcut (jar, cannot be set)
* lib/The following * .jar files
Usage library (commons-cli, commons-compress, Velocity, JAI)

#### ePub 3 template
* template/*
ePub 3 template
* template/OPS/css /*. css
ePub 3 style

#### Conversion Configuration File
* chuki_tag_suf.txt
Convert Forward Lookup Notes to Start End Notes
* chuki_tag.txt
Convert notes to ePub tags
* chuki_alt.txt
Convert Private Text Notes to Alternate Text
* chuki_utf.txt
Convert Private Character Notes (No code) to UTF -8 characters
* chuki_ivs.txt
Convert Private Character Notes (No code) to UTF -8 characters with IVS
* chuki_latin.txt
Convert Latin Text Notes to UTF -8
* replace.txt
character substitution configuration file

#### Web novel configuration file
* web/domain _ name/extract.txt
Wev novel extract definition file

#### Private Character Font File
* gaiji/*
Displays your private characters in their corresponding fonts by placing a single-character font file.

corresponding note
------------
#### Configuration file for basic notes
See chuki_tag.txt

*Response status by model
Horizontal annotations not supported on Kindle

#### Exceptionally Programmed
— Middle left and right of the page
-［＃注記付き］ Convert [# Noted] ○ [# end with "△" note] and [# "○" and "△" readings] to | ○ < △ >
- ［＃「○」に×傍点］ - > Convert to the same number of ruby characters
- Suppress［＃ここで字下げ終わり］for continuous indentation
- Indentation calculation with indentation and indentation numerically
［＃ここから○字下げ、折り返して●字下げ］［＃ここから○字下げ、●字詰め］
- Indentation compound combines classes (Ruled, centered)
- Images　［＃説明（ファイル名.拡張子）］ <img src="ファイル名"/>[# Description (file name.extension)]
& lt; img src = "Filename"/& gt;
- Suppress horizontal text and automatic tate-chu-yoko
- Add New Line for Warichu
- Original text: Page break with (No preceding page break)

Supported Private and Special Characters
------------
* Code-converted Private Character Notes output as UTF -8 characters (UTF8 code, JIS code available)
Notes on External Characters of Aozora Bunko
 ※［＃「さんずい＋垂」、unicode6DB6］ *[# "Sanzui and Tare", unicode6DB6]
※［＃「さんずい＋垂」、U+6DB6、235-7］　*[# "Sanzui and Tare", U + 6 DB6, 235 -7]
 ※［＃「さんずい＋垂」、UCS6DB6、235-7］　*[# "Sanzui and Tare", UCS6DB6, 235 -7]
 ※［＃「てへん＋劣」、第3水準1-84-77］*[# "Tehen + Rare", Level 3 1 -84 -77]
Code Only Private Character Notes
※［＃U+845b］　*[# U + 845 b]
- ※［＃u+845b-e0100］* [# u + 845 b - e0100]
- ※［＃U+845b-U+e0100］　*[# U + 845 b - U + e0100]
— Gaiji notes with no code description
Converts note names to UTF -8 in the correspondence table (chuk _ utf.txt, chuki _ ivs.txt)
IVS characters can be set to output

* Gaiji notes not in UTF -8 output alternate characters (chuk _ alt.txt)

* Aozora Bunko Special Characters ([] [] <<>> | # *)
［＃始め二重山括弧、1-1-52］　*[# begin double angle bracket, 1 -1 -52]　 → 　《
※［＃終わり二重山括弧、1-1-53］ 　*[# close double angle bracket, 1 -1 -53] → 　》
※［＃始め角括弧、1-1-46］　*[# opening bracket, 1 -1 -46] → ［
※［＃終わり角括弧、1-1-47］　*[# close bracket, 1 -1 -47] →　］
※［＃始めきっこう（亀甲）括弧、1-1-44］　*[# Opening bracket (turtle shell), 1 -1 -44] →　〔
※［＃終わりきっこう（亀甲）括弧、1-1-45］　*[# close (turtle shell) bracket, 1 -1 -45] →　〕
※［＃縦線、1-1-35］ 　*[# Vertical Line, 1 -1 -35] → ｜
※［＃井げた、1-1-84］　*[# Igeta, 1 -1 -84] →　＃
※［＃米印、1-2-8］ 　*[# rice sign, 1 -2 -8] → ※

* Output Chinese dots "/\" "/ ′ ′\" in UTF -8


original correspondence note
------------
- Circle from here.
- Separator line
— Empty line
- Center
- Center
— Strikethrough
- Double strikethrough - same as strikethrough
— Page left
- Page Left
— Bottom left of page
— Bottom left of page
- Masatate

Unaddressed Notes
------------
- Correction and "Mom." - > Ignore
- Left Ruby.
- Ground in line to next line
-2 Columns

Scheduled Updates and Revision History
------------
See README_Changes.txt

License
------------
- SourceCode and Binary
GPL v3 ( http://www.gnu.org/licenses/gpl-3.0.html )

- Converted Data
Copyright of converted ePub file will be the same as the input data.
modification and distribution of ePub files can be freely carried out in a copyright.
