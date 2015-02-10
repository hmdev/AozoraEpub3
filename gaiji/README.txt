外字用１文字フォント格納パス
============

説明
------------
このパスに１文字フォントファイルを配置することで、１文字フォントファイル名に
対応した文字を１文字フォントで表示します。

１文字フォントは http://glyphwiki.org/wiki/ からダウンロードできます。
１文字フォント利用時は文字は１文字フォント対応文字の 〓 に変換されてePubに出力します。

ファイル名はGlyphWikiの１文字フォントの形式（小文字）のみ対応になります。
  通常の文字: "u"+ Unicode +".ttf"
  IVS付きの文字: "u"+ Unicode +"-u"+ IVSコード +".ttf"
例:
※［＃「叢－取」、U+4E35］ → u4e35.ttf
※［＃U+4E35］ → u4e35.ttf
※［＃「王＋車」、U+2496D］→ u2496d.ttf
※［＃U+2496D］→ u2496d.ttf
※［＃「にんべん＋誨のつくり」］ → u4fae-ue0101.ttf (侮の異体字)
※［＃U+4FAE-E0101］ → u4fae-ue0101.ttf (侮の異体字)


フォントの調整
------------
GlyphWikiの１文字フォントのそのまま利用で、上下に余白が出たり細すぎる場合は、
以下の手順で修正できます。

####fontforgeの画面上での操作

１．fontforgeをインストール
  fontforge: http://fontforge.github.io/ja/
  Windows: http://www.geocities.jp/meir000/fontforge/
２．フォントを選択
  起動時に１文字フォントファイルを開く
  エンコーディング → 定義済のグリフのみ表示 → フォントを選択
３．u0020とu3000を選択してカット
４．エレメント → フォント情報 → 一般情報 → 縦書きメトリックが存在 を選択 → OK
５．太くする場合はウェイト変更
  エレメント → スタイル→ウェイトを変更 → Embolden by [6]～[12]ぐらいを指定 → OK
  ※ウェイトは端末のフォントに合わせて若干太くすると違和感が無くなります
６．出力
  エレメント → 座標を丸める → 整数に
  ファイル → フォントを出力 → OK


####スクリプトでの一括変換 (Windowsの場合)

１．複数の１文字フォントを gaiji 以外の適当なフォルダに保存
２．１文字フォントのフォルダに gaiji/convert.pe をコピー
３．１文字フォントのフォルダをコマンドプロンプトで開く
４．スクリプトを実行(フォントのあるフォルダで実行すること)
  [fontforgeのパス]fontforge.exe -script convert.pe [AozoraEpub3のインストールパス]\gaiji *.ttf 
  例: c:\fontforge-cygwin\cygwin\bin\fontforge.exe -script convert.pe "c:\Program Files\AozoraEpub3\gaiji" *.ttf

* エラーが出た場合
  該当フォントをfontforgeで確認して重なり等を修正する
  または convert.pe 内の ExpandStroke() の値を調整
