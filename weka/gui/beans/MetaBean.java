/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    MetaBean.java
 *    Copyright (C) 2005 Mark Hall
 *
 */

package weka.gui.beans;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Dimension;
import java.io.Serializable;
import java.beans.EventSetDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;

import weka.gui.Logger;

public class MetaBean extends JPanel 
implements BeanCommon, Visible, EventConstraints,
           Serializable, UserRequestAcceptor {

  protected BeanVisual m_visual = 
    new BeanVisual("Group",
		   BeanVisual.ICON_PATH+"DiamondPlain.gif",
		   BeanVisual.ICON_PATH+"DiamondPlain.gif");

  private transient Logger m_log = null;

  protected Vector m_subFlow = new Vector();
  protected Vector m_inputs = new Vector();
  protected Vector m_outputs = new Vector();

  // the internal connections for the grouping
  protected Vector m_associatedConnections = new Vector();

  public MetaBean() {
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);
  }

  public void setAssociatedConnections(Vector ac) {
    m_associatedConnections = ac;
  }

  public Vector getAssociatedConnections() {
    return m_associatedConnections;
  }

  public void setSubFlow(Vector sub) {
    m_subFlow = sub;
  }

  public void setInputs(Vector inputs) {
    m_inputs = inputs;
  }

  public void setOutputs(Vector outputs) {
    m_outputs = outputs;
  }

  private Vector getBeans(Vector beans, int type) {
    Vector comps = new Vector();
    for (int i = 0; i < beans.size(); i++) {
      BeanInstance temp = (BeanInstance)beans.elementAt(i);
      // need to check for sub MetaBean!
      if (temp.getBean() instanceof MetaBean) {
        switch (type) {
        case 0 : 
          comps.addAll(((MetaBean)temp.getBean()).getBeansInSubFlow());
          break;
        case 1 : 
          comps.addAll(((MetaBean)temp.getBean()).getBeansInInputs());
          break;
        case 2:
          comps.addAll(((MetaBean)temp.getBean()).getBeansInOutputs());
          break;
        }
      } else {
        comps.add(temp);
      }
    }
    return comps;
  }

  /**
   * Return all the beans in the sub flow
   *
   * @return a Vector of all the beans in the sub flow
   */
  public Vector getBeansInSubFlow() {
    return getBeans(m_subFlow, 0);
  }

  /**
   * Return all the beans in the inputs
   *
   * @return a Vector of all the beans in the inputs
   */
  public Vector getBeansInInputs() {
    return getBeans(m_inputs, 1);
  }

  /**
   * Return all the beans in the outputs
   *
   * @return a Vector of all the beans in the outputs
   */
  public Vector getBeansInOutputs() {
    return getBeans(m_outputs, 2);
  }

  private Vector getBeanInfos(Vector beans, int type) {
    Vector infos = new Vector();
    for (int i = 0; i < beans.size(); i++) {
      BeanInstance temp = (BeanInstance)beans.elementAt(i);
      if (temp.getBean() instanceof MetaBean) {
        switch (type) {
        case 0: 
          infos.addAll(((MetaBean)temp.getBean()).getBeanInfoSubFlow());
          break;
        case 1: 
          infos.addAll(((MetaBean)temp.getBean()).getBeanInfoInputs());
          break;
        case 2:
          infos.addAll(((MetaBean)temp.getBean()).getBeanInfoOutputs());
        }
      } else {
        try {
          infos.add(Introspector.getBeanInfo(temp.getBean().getClass()));
        } catch (IntrospectionException ex) {
          ex.printStackTrace();
        }
      }
    }
    return infos;
  }

  public Vector getBeanInfoSubFlow() {
    return getBeanInfos(m_subFlow, 0);
  }

  public Vector getBeanInfoInputs() {
    return getBeanInfos(m_inputs, 1);
  }

  public Vector getBeanInfoOutputs() {
    return getBeanInfos(m_outputs, 2);
  }

  // stores the original position of the input and
  // output beans when this group is created. Used
  // to restore their locations if the group is ungrouped.
  private Vector m_originalCoordsInputs;
  private Vector m_originalCoordsOutputs;
  /**
   * Move coords of all inputs and outputs of this meta bean
   * to the coords of the supplied BeanInstance. Typically
   * the supplied BeanInstance is the BeanInstance that encapsulates
   * this meta bean; the result in this case is that all inputs
   * and outputs are shifted so that their coords coincide with
   * the meta bean and all connections to them appear (visually) to
   * go to/from the meta bean.
   *
   * @param toShiftTo the BeanInstance whos coordinates will
   * be used.
   * @param save true if coordinates are to be saved.
   */
  public void shiftInputsAndOutputs(BeanInstance toShiftTo, 
                                    boolean save) {
    if (save) {
      m_originalCoordsInputs = new Vector();
      m_originalCoordsOutputs = new Vector();
    }
    int targetX = toShiftTo.getX();
    int targetY = toShiftTo.getY();

    for (int i = 0; i < m_inputs.size(); i++) {
      BeanInstance temp = (BeanInstance)m_inputs.elementAt(i);
      if (save) {
        Point p = new Point(temp.getX(), temp.getY());
        m_originalCoordsInputs.add(p);
      }
      temp.setX(targetX); temp.setY(targetY);
    }

    for (int i = 0; i < m_outputs.size(); i++) {
      BeanInstance temp = (BeanInstance)m_outputs.elementAt(i);
      if (save) {
        Point p = new Point(temp.getX(), temp.getY());
        m_originalCoordsOutputs.add(p);
      }
      temp.setX(targetX); temp.setY(targetY);
    }
  }

  public void restoreInputAndOutputCoords() {
    for (int i = 0; i < m_inputs.size(); i++) {
      BeanInstance temp = (BeanInstance)m_inputs.elementAt(i);
      Point p = (Point)m_originalCoordsInputs.elementAt(i);
      JComponent c = (JComponent)temp.getBean();
      Dimension d = c.getPreferredSize();
      int dx = (int)(d.getWidth() / 2);
      int dy = (int)(d.getHeight() / 2);
      temp.setX((int)p.getX()+dx);
      temp.setY((int)p.getY()+dy);
    }

    for (int i = 0; i < m_outputs.size(); i++) {
      BeanInstance temp = (BeanInstance)m_outputs.elementAt(i);
      Point p = (Point)m_originalCoordsOutputs.elementAt(i);
      JComponent c = (JComponent)temp.getBean();
      Dimension d = c.getPreferredSize();
      int dx = (int)(d.getWidth() / 2);
      int dy = (int)(d.getHeight() / 2);
      temp.setX((int)p.getX()+dx);
      temp.setY((int)p.getY()+dy);
    }
  }

  /**
   * Returns true, if at the current time, the event described by the
   * supplied event descriptor could be generated.
   *
   * @param esd an <code>EventSetDescriptor</code> value
   * @return a <code>boolean</code> value
   */
  public boolean eventGeneratable(EventSetDescriptor esd) {
    String eventName = esd.getName();
    return eventGeneratable(eventName);
  }

  /**
   * Returns true, if at the current time, the named event could
   * be generated. Assumes that the supplied event name is
   * an event that could be generated by this bean
   *
   * @param eventName the name of the event in question
   * @return true if the named event could be generated at this point in
   * time
   */
  public boolean eventGeneratable(String eventName) {
    for (int i = 0; i < m_outputs.size(); i++) {
      BeanInstance output = (BeanInstance)m_outputs.elementAt(i);
      if (output.getBean() instanceof EventConstraints) {
        if (((EventConstraints)output.getBean()).eventGeneratable(eventName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if, at this time, 
   * the object will accept a connection with respect to the
   * supplied EventSetDescriptor
   *
   * @param esd the EventSetDescriptor
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(EventSetDescriptor esd) {
    Vector targets = getSuitableTargets(esd);
    for (int i = 0; i < targets.size(); i++) {
      BeanInstance input = (BeanInstance)targets.elementAt(i);
      if (input.getBean() instanceof BeanCommon) {
        if (((BeanCommon)input.getBean()).connectionAllowed(esd)) {
          return true;
        }
      } else {
        return true;
      }
    }
    return false;
  }

  public boolean connectionAllowed(String eventName) {
    return false;
  }

  /**
   * Notify this object that it has been registered as a listener with
   * a source with respect to the named event. This is just a dummy
   * method in this class to satisfy the interface. Specific code
   * in BeanConnection takes care of this method for MetaBeans
   *
   * @param eventName the event
   * @param source the source with which this object has been registered as
   * a listener
   */
  public synchronized void connectionNotification(String eventName,
						  Object source) {
  }
  
  /**
   * Notify this object that it has been deregistered as a listener with
   * a source with respect to the supplied event name. This is just a dummy
   * method in this class to satisfy the interface. Specific code
   * in BeanConnection takes care of this method for MetaBeans
   *
   * @param eventName the event
   * @param source the source with which this object has been registered as
   * a listener
   */
  public synchronized void disconnectionNotification(String eventName,
						     Object source) {

  }

  /**
   * Stop all encapsulated beans
   */
  public void stop() {
    for (int i = 0; i < m_inputs.size(); i++) {
      Object temp = m_inputs.elementAt(i);
      if (temp instanceof BeanCommon) {
        ((BeanCommon)temp).stop();
      }
    }
  }

  /**
   * Sets the visual appearance of this wrapper bean
   *
   * @param newVisual a <code>BeanVisual</code> value
   */
  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }

  /**
   * Gets the visual appearance of this wrapper bean
   */
  public BeanVisual getVisual() {
    return m_visual;
  }

  /**
   * Use the default visual appearance for this bean
   */
  public void useDefaultVisual() {
    m_visual.loadIcons(BeanVisual.ICON_PATH+"DiamondPlain.gif",
		       BeanVisual.ICON_PATH+"DiamondPlain.gif");
  }

  /**
   * Return an enumeration of requests that can be made by the user
   *
   * @return an <code>Enumeration</code> value
   */
  public Enumeration enumerateRequests() {
    Vector newVector = new Vector();
    for (int i = 0; i < m_subFlow.size(); i++) {
      BeanInstance temp = (BeanInstance)m_subFlow.elementAt(i);
      if (temp.getBean() instanceof UserRequestAcceptor) {
        String prefix = (temp.getBean() instanceof WekaWrapper)
          ? ((WekaWrapper)temp.getBean()).getWrappedAlgorithm().getClass().getName()
          : temp.getBean().getClass().getName();
        prefix = prefix.substring(prefix.lastIndexOf('.')+1, prefix.length());
        prefix = ""+(i+1)+": ("+prefix+")";
        Enumeration en = ((UserRequestAcceptor)temp.getBean()).enumerateRequests();
        while (en.hasMoreElements()) {
          String req = (String)en.nextElement();
          if (req.charAt(0) == '$') {
            prefix = '$'+prefix;
            req = req.substring(1, req.length());
          }
          newVector.add(prefix+" "+req);
        }          
      }
    }
    
    return newVector.elements();
  }

  /**
   * Perform a particular request
   *
   * @param request the request to perform
   * @exception IllegalArgumentException if an error occurs
   */
  public void performRequest(String request) {
    // first grab the index if any
    if (request.indexOf(":") < 0) {
      return;
    }
    String tempI = request.substring(0, request.indexOf(':'));
    int index = Integer.parseInt(tempI);
    index--;
    String req = request.substring(request.indexOf(')')+1, 
                                   request.length()).trim();
    UserRequestAcceptor target = 
      (UserRequestAcceptor)(((BeanInstance)m_subFlow.elementAt(index)).getBean());
    target.performRequest(req);
                                   
  }

  /**
   * Set a logger
   *
   * @param logger a <code>Logger</code> value
   */
  public void setLog(Logger logger) {
    m_log = logger;
  }

  public void removePropertyChangeListenersSubFlow(PropertyChangeListener pcl) {
    for (int i = 0; i < m_subFlow.size(); i++) {
      BeanInstance temp = (BeanInstance)m_subFlow.elementAt(i);
      if (temp.getBean() instanceof Visible) {
        ((Visible)(temp.getBean())).getVisual().
          removePropertyChangeListener(pcl);
      }
      if (temp.getBean() instanceof MetaBean) {
        ((MetaBean)temp.getBean()).removePropertyChangeListenersSubFlow(pcl);
      }
    }
  }

  public void addPropertyChangeListenersSubFlow(PropertyChangeListener pcl) {
    for (int i = 0; i < m_subFlow.size(); i++) {
      BeanInstance temp = (BeanInstance)m_subFlow.elementAt(i);
      if (temp.getBean() instanceof Visible) {
        ((Visible)(temp.getBean())).getVisual().
          addPropertyChangeListener(pcl);
      }
      if (temp.getBean() instanceof MetaBean) {
        ((MetaBean)temp.getBean()).addPropertyChangeListenersSubFlow(pcl);
      }
    }
  }

  /**
   * Checks to see if any of the inputs to this group implements
   * the supplied listener class
   *
   * @param listenerClass the listener to check for
   */
  public boolean canAcceptConnection(Class listenerClass) {
    for (int i = 0; i < m_inputs.size(); i++) {
      BeanInstance input = (BeanInstance)m_inputs.elementAt(i);
      if (listenerClass.isInstance(input.getBean())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return a list of input beans capable of receiving the 
   * supplied event
   *
   * @param esd the event in question
   * @return a vector of beans capable of handling the event
   */
  public Vector getSuitableTargets(EventSetDescriptor esd) {
    Class listenerClass = esd.getListenerType(); // class of the listener
    Vector targets = new Vector();
    for (int i = 0; i < m_inputs.size(); i++) {
      BeanInstance input = (BeanInstance)m_inputs.elementAt(i);
      if (listenerClass.isInstance(input.getBean())) {
        targets.add(input);
      }
    }
    return targets;
  }
}
