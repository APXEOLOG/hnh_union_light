package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHWPanel extends Widget implements IHWindowParent {

    public static ChatHWPanel instance;
    static BufferedImage[] fbtni = new BufferedImage[] {
	    Resource.loadimg("gfx/hud/fbtn"),
	    Resource.loadimg("gfx/hud/fbtnd"),
	    Resource.loadimg("gfx/hud/fbtnh") };
    static final int minbtnw = 90;
    static final int maxbtnw = 120;
    static final int sbtnw = 50;
    static final int btnh = 40;
    static final Coord minsz = new Coord(125, 125);
    HWindow awnd;
    List<HWindow> wnds = new ArrayList<HWindow>();
    Map<HWindow, Button> btns = new HashMap<HWindow, Button>();
    Button sub, sdb;
    IButton fbtn;
    int urgency, woff = 0;
    boolean folded = false, dm = false;
    Coord btnc, doff;

    public ChatHWPanel(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent);
	instance = this;
	btnc = sz.sub(new Coord(sz.x, btnh));
	sub = new Button(new Coord(300, 260), sbtnw, this,
		Resource.loadimg("gfx/hud/slen/sau")) {
	    public void click() {
		sup();
	    }
	};
	sdb = new Button(new Coord(300, 280), sbtnw, this,
		Resource.loadimg("gfx/hud/slen/sad")) {
	    public void click() {
		sdn();
	    }
	};
	sub.visible = sdb.visible = false;
	fbtn = new IButton(Coord.z, this, fbtni[0], fbtni[1], fbtni[2]);
    }

    private void sup() {
	woff--;
	updbtns();
    }

    private void sdn() {
	woff++;
	updbtns();
    }

    public void draw(GOut g) {
	Coord c = folded ? new Coord(0, 245) : Coord.z;
	fbtn.c = c;
	g.chcolor(220, 220, 200, folded?32:200);
	g.frect(c, sz.sub(c));
	if(folded)
	    g.chcolor(255,255,255,160);
	else
	    g.chcolor();
	super.draw(g);
	g.chcolor(64, 64, 64, folded?32:255);
	g.rect(c, sz.add(new Coord(1, 1).sub(c)));
	g.chcolor();
	if ((folded) && (SlenHud.urgcols[urgency] != null)) {
	    g.chcolor(SlenHud.urgcols[urgency]);
	    g.image(fbtni[0], c);
	    g.chcolor();
	}
    }

    private void updbtns() {
	int k = (sz.x - sbtnw)/minbtnw;
	if(k > wnds.size()/2) {
	    k = Math.max(wnds.size()/2, 1);
	    if ((wnds.size()%2) != 0) 
		k++;
	}
	int bw = Math.min((sz.x - sbtnw)/k, maxbtnw);
	int bpp = 2*k;
	
	if (wnds.size() <= bpp) {
	    woff = 0;
	} else {
	    if (woff < 0)
		woff = 0;
	    if (woff > wnds.size() - bpp)
		woff = wnds.size() - bpp;
	}
	for (Button b : btns.values())
	    b.visible = false;
	sub.visible = sdb.visible = false;
	for (int i = 0; i < bpp; i++) {
	    int wi = i + woff;
	    if (wi >= wnds.size())
		continue;
	    if (woff > 0) {
		sub.visible = true;
		sub.c = btnc.add(new Coord(sz.x - sbtnw, 0));
	    }
	    if (woff < wnds.size() - bpp) {
		sdb.visible = true;
		sdb.c = btnc.add(new Coord(sz.x - sbtnw, 20));
	    }
	    HWindow w = wnds.get(wi);
	    Button b = btns.get(w);
	    w.sz = sz.sub(0, btnh);
	    b.change(w.title, w.visible ? Color.WHITE
		    : SlenHud.urgcols[w.urgent]);
	    b.visible = true;
	    b.sz.x = bw;
	    b.c = btnc.add(new Coord(bw * (i % k), ((int) i / k) * 20));
	}
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if (sender == fbtn) {
	    folded = !folded;
	    if(awnd != null)
		awnd.visible = !folded;
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
    
    @Override
    public void addwnd(final HWindow wnd) {
	fbtn.raise();
	wnd.sz = sz.sub(new Coord(0, 40));
	wnd.c = Coord.z;
	wnds.add(wnd);
	btns.put(wnd, new Button(new Coord(0, 260), 100, this, wnd.title) {
	    public void click() {
		setawnd(wnd, true);
	    }
	});
	if(!folded)
	    setawnd(wnd);
	else
	    wnd.visible = false;
	    updbtns();
    }

    @Override
    public void remwnd(HWindow wnd) {
	if (wnd == awnd) {
	    int i = wnds.indexOf(wnd);
	    if (wnds.size() == 1)
		setawnd(null);
	    else if (i < 0)
		setawnd(wnds.get(0));
	    else if (i >= wnds.size() - 1)
		setawnd(wnds.get(i - 1));
	    else
		setawnd(wnds.get(i + 1));
	}
	wnds.remove(wnd);
	ui.destroy(btns.get(wnd));
	btns.remove(wnd);
	updbtns();
    }

    @Override
    public void updurgency(HWindow wnd, int level) {
	if ((wnd == awnd) && !folded)
	    level = -1;
	if (level == -1) {
	    if (wnd.urgent == 0)
		return;
	    wnd.urgent = 0;
	} else {
	    if (wnd.urgent >= level)
		return;
	    wnd.urgent = level;
	}
	Button b = btns.get(wnd);
	b.change(wnd.title, SlenHud.urgcols[wnd.urgent]);
	int max = 0;
	for (HWindow w : wnds) {
	    if (w.urgent > max)
		max = w.urgent;
	}
	urgency = (level>0)?level:0;
    }

    @Override
    public void setawnd(HWindow wnd) {
	setawnd(wnd, false);
    }

    @Override
    public void setawnd(HWindow wnd, boolean focus) {
	if (focus)
	    folded = false;
	awnd = wnd;
	for (HWindow w : wnds)
	    w.visible = false;
	if (wnd != null) {
	    wnd.visible = !folded;
	    updurgency(wnd, -1);
	}
	updbtns();
    }
    
    public boolean mousedown(Coord c, int button) {
	parent.setfocus(this);
	raise();
	if(super.mousedown(c, button))
	    return(true);
	if(!c.isect(Coord.z, sz))
	    return(false);
	if(button == 1) {
	    ui.grabmouse(this);
	    dm = true;
	    doff = c;
	}
	return(true);
    }
	
    public boolean mouseup(Coord c, int button) {
	if(dm) {
	    ui.grabmouse(null);
	    dm = false;
	} else {
	    super.mouseup(c, button);
	}
	return(true);
    }
	
    public void mousemove(Coord c) {
	if(dm) {
	    this.c = this.c.add(c.add(doff.inv()));
	} else {
	    super.mousemove(c);
	}
    }
}