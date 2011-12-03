package union;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import union.INIFile.Pair;

import haven.*;

/*
 *  Created by APXEOLOG, edited by Kerrigan
 */

public class UnionUtils {
	/* Draw curiosities */
	public static Set<Pair<Coord, Color>> profits = new HashSet<Pair<Coord, Color>>();
	public static ArrayList<Pair<String, Color>> minimap_highlights = new ArrayList<Pair<String, Color>>();
	public static int playerId = 0;

	public static void loadCuriosityInfo() {
		try {
			INIFile ifile = new INIFile("haven.ini");
			minimap_highlights = ifile.getSectionColors("HIGHLIGHT", "");
		} catch (IOException e) {
			// Cannot open file
		}
	}

	public static void drawProfitMinimap(GOut g, Coord tc, Coord hsz) {
		if (UI.instance.minimap == null)
			return;
		synchronized (profits) {
			for (Pair<Coord, Color> arg : profits) {
				Coord ptc = arg.fst.div(MCache.tilesz).add(tc.inv())
						.add(hsz.div(2));
				g.chcolor(Color.BLACK);
				g.fellipse(ptc, new Coord(5, 5));
				g.chcolor(arg.snd);
				g.fellipse(ptc, new Coord(3, 3));
				g.chcolor();
			}
		}
	}

	public static void updateProfits(OCache oc, GOut g) {
		try {
			profits.clear();
			String name;
			g.chcolor(255, 153, 51, 96);
			synchronized (oc) {
				for (Gob tg : oc) {
					name = tg.resname();
					if (tg.sc != null) {
						int hit = 0;
						for (Pair<String, Color> pp : minimap_highlights) {
							if (name.contains(pp.fst)
									&& !tg.getres().name.contains("/cdv")) {
								profits.add(new Pair<Coord, Color>(tg.rc,
										pp.snd));
								hit++;
							}
						}
						if (hit > 0) UI.instance.mainview.drawradius(g, tg.sc, 30);
					}
				}
			}
		} catch (Exception e) {

		}
	}

	// draw vision square
	public static void drawVisSquare(GOut g, Coord tc, Coord hsz) {
		if (UI.instance.minimap == null)
			return;
		try {
			Coord current = Glob.instance.oc.getgob(playerId) == null ? new Coord(
					0, 0) : Glob.instance.oc.getgob(playerId).rc;
			Coord ptc = current.div(MCache.tilesz).add(tc.inv())
					.add(hsz.div(2));
			g.chcolor(255, 255, 255, 64);
			g.frect(ptc.sub(42, 42), new Coord(85, 85));
			g.chcolor();
		} catch (Throwable t) {
			// Ignore
		}
	}
}
