package org.jprelude.experimental.webui.widget

import com.vaadin.server.FontAwesome
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.CssLayout
import com.vaadin.ui.FormLayout
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.MenuBar
import com.vaadin.ui.TextField
import com.vaadin.ui.themes.ValoTheme
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Button.ClickListener
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.Window
import com.vaadin.event.ShortcutAction.KeyCode

class ToolBar(
    
) extends Widget {
    def render() = {
      val content =  new HorizontalLayout
      content setWidth "100%"
      var hboxb = new HorizontalLayout
      hboxb setSpacing true
      content addComponent hboxb
      
       var headline = new Label()
      headline.setValue("<label class=\"v-label-h1\" style=\"margin-right: 2em\">Products</label>")
      headline.setContentMode(ContentMode.HTML)
      headline.setWidthUndefined()
      hboxb addComponent headline
      hboxb.setComponentAlignment(headline, Alignment.MIDDLE_LEFT)
        
      val hbox1 = new HorizontalLayout
      hbox1.setWidth("100%")
      hbox1 setSpacing true
      
      val hbox2 = new HorizontalLayout
      
      hboxb addComponent hbox1
      content addComponent hbox2
      
      hboxb.setComponentAlignment(hbox1, Alignment.MIDDLE_LEFT)
      content.setComponentAlignment(hbox2, Alignment.MIDDLE_RIGHT)
      
       val group1= new CssLayout;
   
        group1 addStyleName "v-component-group";
        hbox1 addComponent group1;
        
       
        val btnNew = new Button("New")
        btnNew.addStyleName("smalls")
        btnNew.setIcon(FontAwesome.FILE_O)
        group1 addComponent btnNew
        
        val btnEdit = new Button("Edit")
        btnEdit.addStyleName("smalls")
        btnEdit.setIcon(FontAwesome.EDIT)
        group1 addComponent btnEdit
        
        val btnDelete = new Button("Delete")
        btnDelete.addStyleName("smalls")
        btnDelete.setIcon(FontAwesome.TRASH_O)
        group1 addComponent btnDelete
        
        val split = new MenuBar();
        val dropdown = split.addItem("Export", null);
        dropdown setIcon FontAwesome.DOWNLOAD
       
        
        val submenu1 = dropdown.addItem("Export selected", null)
        submenu1.addItem("Export filtered to XML", null);
        submenu1.addItem("Export filtered to CSV", null);
        submenu1.addItem("Export filtered to ODT Spreadsheet", null);
        submenu1.addItem("Export filtered to Excel Spreadsheet", null);
submenu1.setVisible(false)
        val submenu2 = dropdown.addItem("Export all", null)
        submenu2.addItem("Export all to XML", null);
        submenu2.addItem("Export all to CSV", null);
        submenu2.addItem("Export all to ODT Spreadsheet", null);
        submenu2.addItem("Export all to Excel Spreadsheet", null);
submenu2.setVisible(false)
        hbox1 addComponent split

        val advancedFilter = new Button("Advanced Filter")

        advancedFilter setIcon(FontAwesome.FILTER)
        advancedFilter.setStyleName("link")
        advancedFilter addClickListener new ClickListener() {
            override def buttonClick(event: ClickEvent) {
              System.out.println("xxx")
              
            val notificationsWindow = new Window();
            notificationsWindow.setWidth("300px");
            notificationsWindow.addStyleName("notifications");
            notificationsWindow.setClosable(false);
            notificationsWindow.setResizable(false);
            notificationsWindow.setDraggable(false);
            //notificationsWindow.setCloseShortcut(KeyCode.ESCAPE, null);
            //notificationsWindow.setContent(notificationsLayout);
            
             if (!notificationsWindow.isAttached()) {
            notificationsWindow.setPositionY(event.getClientY()
                    - event.getRelativeY() + 40);
            
            notificationsWindow.setPositionX(event.getClientX() - event.getRelativeX() + 10);  
            content.getUI().addWindow(notificationsWindow)
            notificationsWindow.focus();
        } else {
            notificationsWindow.close();
        }
              
           }
        }

        val group = new CssLayout();
        
        val v = new VerticalLayout;
        v.setIcon(FontAwesome.ARROW_CIRCLE_O_RIGHT)
        
        //val btnSearchOptions = new Button()
        
        //btnSearchOptions.setIcon(FontAwesome.SEARCH);
        //btnSearchOptions.setHtmlContentAllowed(true)

                val btnSearchOptions = new MenuBar();
        val dropdown2 = btnSearchOptions.addItem("", null);
        dropdown2 setIcon FontAwesome.SEARCH
       
        
        dropdown2.addItem("Product number", null);
        val x = dropdown2.addItem("Name", null);
        x.setCheckable(true);
        x.setChecked(true)
        dropdown2.addItem("Description", null);
        
        
        
        
        
        
        group addComponent btnSearchOptions
        
        group.addStyleName("v-component-group");
        hbox2.addComponent(group);
        hbox2 addComponent advancedFilter
        
        val filter = new TextField
        filter.setInputPrompt("Search");
      //  filter.setIcon(FontAwesome.SEARCH);
        // filter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);

        
        
        group.addComponent(filter)
     
        val btnGo = new Button
        btnGo setIcon FontAwesome.ARROW_RIGHT
        group addComponent btnGo;
        
        hbox2.setComponentAlignment(group, Alignment.MIDDLE_RIGHT)

      
      
      content
    }
}