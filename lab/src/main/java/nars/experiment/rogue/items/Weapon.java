package nars.experiment.rogue.items;

import nars.experiment.rogue.combat.MeleeAttackType;
import nars.experiment.rogue.creatures.AttribModifier;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Weapon extends Item implements Serializable
{
	public Weapon()
	{
		super();
		melee_attacks= new ArrayList<>();
	}
	
	public Weapon(Element e) throws ParserConfigurationException, SAXException, IOException
	{
		this();
		fillFromElement(e);
	}


	@Override
	protected boolean parseElement(Element e)
	{
		if (super.parseElement(e)) 
			return true;
		String value="";
		Text tnode = (Text)e.getFirstChild();
		if (tnode!=null) 
			value = tnode.getData().trim();
		else 
			return true;
		if (e.getTagName().equals("melee_attack_type"))
			addMeleeAttackType(new MeleeAttackType(e,null));
		else 
			return false;
		return true;
	}
	
	public void addMeleeAttackType (MeleeAttackType mat)
	{
		mat.setHost(this);
		melee_attacks.add(mat);
	}
	 
	public ArrayList<MeleeAttackType> getMeleeAttackTypes()
	{
		return melee_attacks;
	}
	
	 @Override
	 public Item copySingle()
	 {
		Weapon cloned=new Weapon();
		cloned.color=color;
		cloned.weight=weight;
		cloned.name=name;
		cloned.many_name=many_name;
		cloned.price=price;
		cloned.level=level;
		cloned.symbol=symbol;
	 	cloned.inventory_section=inventory_section;
	 	Iterator<MeleeAttackType>  i= melee_attacks.iterator();
	 	while (i.hasNext())
	 		cloned.melee_attacks.add(i.next());
	 	Iterator<AttribModifier>  i2= attrMods.iterator();
	 	while (i2.hasNext())
	 		cloned.attrMods.add(i2.next());
	 	return cloned; 											
	 }
	 
	protected ArrayList<MeleeAttackType> melee_attacks;
}