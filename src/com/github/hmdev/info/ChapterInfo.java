package com.github.hmdev.info;

import java.util.List;
import com.github.hmdev.util.CharUtils;

/** 目次用の章の情報を格納（仮） */
public class ChapterInfo
{
	/** xhtmlファイルのセクション毎の連番ID */
	String sectionId;
	/** 章ID 見出し行につけたspanのID */
	String chapterId;
	/** 章名称 */
	String chapterName;
	
	/** 目次階層レベル */
	public int chapterLevel;
	
	/** 出力前に階層化開始タグを入れる回数 通常は1回 */
	public int levelStart = 0;
	/** 出力後に階層化終了タグを入れる回数 */
	public int levelEnd = 0;
	/** navPointを閉じる回数 */
	public int navClose = 1;
	
	public ChapterInfo(String sectionId, String chapterId, String chapterName, int chapterLevel)
	{
		this.sectionId = sectionId;
		this.chapterId = chapterId;
		this.chapterName = chapterName;
		this.chapterLevel = chapterLevel;
	}

	public String getSectionId()
	{
		return sectionId;
	}
	public void setSectionId(String sectionId)
	{
		this.sectionId = sectionId;
	}

	public String getChapterId()
	{
		return chapterId;
	}
	public void setChapterId(String chapterId)
	{
		this.chapterId = chapterId;
	}

	public String getChapterName()
	{
		return chapterName;
	}
	public void setChapterName(String chapterName)
	{
		this.chapterName = chapterName;
	}
	public String getNoTagChapterName()
	{
		return CharUtils.removeTag(chapterName);
	}
	
	public int getChapterLevel()
	{
		return chapterLevel;
	}
	
	/** Velocityでループするために配列を返す */
	public int[] getLevelStart()
	{
		if (this.levelStart == 0) return null;
		return new int[this.levelStart];
	}
	/** Velocityでループするために配列を返す */
	public int[] getLevelEnd()
	{
		if (this.levelEnd == 0) return null;
		return new int[this.levelEnd];
	}
	/** Velocityでループするために配列を返す */
	public int[] getNavClose()
	{
		if (this.navClose <= 0) return null;
		return new int[this.navClose];
	}

	/**
	 * 目次の階層情報を設定する。
	 *
	 *     chapterLevel: 目次の階層レベルを設定する
	 *     levelStart  : 目次階層が深くなる場合の ol 開始タグの出力回数。親を持つ最初の子ノードに1を設定する。それ以外は0を設定する
	 *     levelEnd    : 目次階層が浅くなる場合の ol 終了タグの出力回数。(A)次のレベルが浅くなるノードと(B)最後のノードに設定する
	 *                   (A)には次のノードとのレベル差、(B)には自分のレベルを設定する
	 *     navClose    : navPoint 終了タグの出力回数。 子ノードを持つノードの場合は 0 として、それ以外の場合は levelEnd + 1 とする
	 *
	 * @param navNest
	 * @param ncxNest
	 * @param chapterInfos
	 * @param insertTitleToc
	 */
	public static void setTocNestLevel(boolean navNest, boolean ncxNest, List<ChapterInfo> chapterInfos, boolean insertTitleToc)
	{
		if (chapterInfos.size() < 1) {
			return;
		}

		//表題のレベルを2つめと同じにする
		if (insertTitleToc && chapterInfos.size() >= 2) {
			chapterInfos.get(0).chapterLevel = chapterInfos.get(1).chapterLevel;
		}

		// 見出しレベルをコピーする
		int[] levels = new int[chapterInfos.size()];
		for (int i = 0; i < chapterInfos.size(); i++) {
			levels[i] = chapterInfos.get(i).chapterLevel;
		}

		//----------------------------------------------
		// 目次の階層レベルの設定
		//----------------------------------------------
		// (参考)
		// 以下、逆順にループすれば int配列（levels）を作らずに済みそうだが int配列を作ったほうが処理が速い、かつ分かりやすい
		// また chapterInfos の具象クラスは java.util.Vector のため、入れ子でループすると余計に遅くなる
		for (int i = 0; i < chapterInfos.size(); i++) {
			int count = 0;

			//現在のレベルを取得
			int curr_level = levels[i];
			// 親の数を数える
			for (int j = i - 1; j >= 0; j--) {
				if (levels[j] < curr_level) {
					count++;
					// 親のレベルを現在のレベルにする
					curr_level = levels[j];
				}
			}
			// 親の数がそのノードの深さ（レベル）となる
			chapterInfos.get(i).chapterLevel = count;
		}

		//----------------------------------------------
		// Velocity用のフィールド設定
		//----------------------------------------------
		ChapterInfo curr = chapterInfos.get(0);
		curr.levelStart = 0;

		ChapterInfo prev = curr;

		// ループは 2番目の要素から開始
		for (int i = 1; i < chapterInfos.size(); i++) {
			curr = chapterInfos.get(i);
			assert prev != null;

			int diff = curr.chapterLevel - prev.chapterLevel;
			assert diff <= 1; //上記の処理を経て diff > 1 となることはない
			if (diff > 0) {
				// 前より深い場合
				assert diff == 1;
				curr.levelStart = diff;
				prev.levelEnd = 0;
			} else {
				// 前より深くない場合
				assert curr.chapterLevel <= prev.chapterLevel;
				curr.levelStart = 0;
				prev.levelEnd = -diff;
			}
			prev = curr;
		}
		curr.levelEnd = curr.chapterLevel;

		if (ncxNest) {
			prev = null;
			for (int i = 0; i < chapterInfos.size(); i++) {
				curr = chapterInfos.get(i);
				curr.navClose = curr.levelEnd + 1;
				if (curr.levelStart > 0 && prev != null) {
					//前が親ノードなら、前のノードの navClose を 0 にする
					prev.navClose = 0;
				}
				prev = curr;
			}
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{chapterLevel: ").append(chapterLevel);
		sb.append(", levelStart: ").append(levelStart);
		sb.append(", levelEnd: ").append(levelEnd);
		sb.append(", navClose: ").append(navClose);
		sb.append(", sectionId: \"").append(sectionId);
		sb.append("\", chapterId: \"").append(chapterId);
		sb.append("\", chapterName: \"").append(chapterName);
		sb.append("\"}");
		return sb.toString();
	}
}
