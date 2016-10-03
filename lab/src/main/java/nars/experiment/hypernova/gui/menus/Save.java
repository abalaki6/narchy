package nars.experiment.hypernova.gui.menus;

import nars.experiment.hypernova.SaveGame;
import nars.experiment.hypernova.Universe;
import nars.experiment.hypernova.gui.Menu;
import nars.experiment.hypernova.gui.MenuScreen;

public class Save extends MenuScreen
{
  public void loadMenu() {
    for(int i = 1; i <= 5; i ++)
      addItem(Alignment.LEFT, SaveGame.saveStats(i), null, i);
    
    addItem(Alignment.RIGHT, "Back", null, 6);
  }
 
  public void functions(int func, String value) {
    if(func <= 5)
    {
      Universe.get().togglePause(false);
      Menu.setInMenu(false);
      SaveGame.save(func, Menu.getScreenshot());
    } else back();
  }
}