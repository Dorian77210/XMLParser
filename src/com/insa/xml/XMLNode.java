package com.insa.xml;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author Dorian Terbah
 */
public class XMLNode
{
    private String tag;
    private String text;
    private Map<String, String> attributes;
    private boolean isSelfClosing;
    
    private XMLNode parent;
    private XMLNode nextSibling;
    private XMLNode previousSibling;
    private List<XMLNode> children;
    
    int level;
    
    private XMLNode(XMLNode node)
    {
        this.tag = new String(node.tag);
        this.text = new String(node.text);
        this.isSelfClosing = node.isSelfClosing;
        this.attributes = new LinkedHashMap<>();
        this.children = new ArrayList<>();
        this.level = level;
        
        // clone the attributes
        for (Map.Entry<String, String> entry : node.attributes.entrySet())
        {
            String key = new String(entry.getKey());
            String value = new String(entry.getValue());
            this.attributes.put(key, value);
        }
        
        for (XMLNode child : node.children)
        {
            this.children.add(child.clone());
        }
        
        this.parent = node.parent.clone();
        this.previousSibling = node.previousSibling.clone();
        this.nextSibling = node.nextSibling.clone();
    }
    
    public XMLNode()
    {
        this(false, 0);
    }
    
    public XMLNode(boolean isSelfClosing, int level)
    {
        this.tag = "";
        this.level = level;
        this.text = "";
        this.isSelfClosing = isSelfClosing;
        this.attributes = new LinkedHashMap<>();
        this.parent = null;
        
        this.children = new ArrayList<>();
        this.previousSibling = null;
        this.nextSibling = null;
    }
    
    public XMLNode(boolean isSelfClosing, XMLNode parent)
    {
        this(isSelfClosing, 0);
        this.parent = parent;
    }
    
    /**
     * Clone the current XMLNode 
     * @return The new copy of the current XMLNode
     */
    @Override
    public XMLNode clone()
    {
        return new XMLNode(this);
    }
    
    /**
     * Set the next sibling of the current node
     * @param node The node to append
     * @return The current node
     */
    public XMLNode setNextSibling(XMLNode node)
    {
        if (node != null)
        {   
            node.previousSibling = this;
            node.parent = this.parent;
            node.level = this.level;
        }
        
        this.nextSibling = node;
        
        return this;
    }
    
    /**
     * Get the next sibling of the current node
     * @return The next sibling of the current node
     */
    public XMLNode next()
    {
        return this.nextSibling;
    }
    
    /**
     * Set the previous sibling of the current node
     * @param node The node to append
     * @return The current node
     */
    public XMLNode setPreviousSibling(XMLNode node)
    {
        XMLNode currentSibling = this.previousSibling;
        if (node != null)
        {
            node.nextSibling = currentSibling;
            node.parent = this.parent;
            node.level = this.level;
        }
        
        this.previousSibling = node;
        return this;
    }
    
    /**
     * Get the previous node of the current node.
     * @return The previous node of the current node
     */
    public XMLNode previous()
    {
        return this.previousSibling;
    }
    
    /**
     * Append a child for the current node
     * @param node The node to append
     * @return The current node
     */
    public XMLNode appendChild(XMLNode node)
    {
        this.children.add(node);
        node.parent = this;
        node.level = this.level + 1;
        return this;
    }
    
    /**
     * Append children to the current node.
     * @param nodes The children to append.
     * @return  The current node.
     */
    public XMLNode appendChildren(List<XMLNode> nodes)
    {
        for (XMLNode node : nodes)
        {
            this.appendChild(node);
        }
        
        return this;
    }
    
    /**
     * Create a node with a tag and a text to the current node.
     * @param tag The tag of the new child.
     * @param text The text of the new child.
     * @return The new child.
     */
    public XMLNode createAndAppendChild(String tag, String text)
    {
        XMLNode node = new XMLNode(false, this);
        
        // configuration of the new node
        node.level = this.level + 1;
        node.tag = tag;
        node.text = text;
        this.children.add(node);
        
        return node;
    }
    
    /**
     * Remove the siblings of the current node according by their tag name.
     * The deletion doesn't take the deep nodes.
     * @param tag The tag of the nodes to remove
     * @return The removed nodes
     */
    public List<XMLNode> removeSiblingsByTag(String tag)
    {
        List<XMLNode> removedSiblings = this.getSiblingsByTag(tag);
        XMLNode node = this, next, previous;
        
        // next siblgins
        while (node != null)
        {
            next = node.nextSibling;
            previous = node.previousSibling;

            if (node.tag.equals(tag))
            {
                // to delete
                if (next != null)
                {
                    if (previous != null)
                    {
                        previous.nextSibling = next;
                    }
                    
                    next.previousSibling = previous;
                }
                
                removedSiblings.add(node);
            }
            
            node = next;
        }
        
        // previous sibling
        node = this.previousSibling;
        while (node != null)
        {
            next = node.nextSibling;
            previous = node.previousSibling;
            if (node.tag.equals(tag))
            {
                // to delete
                if (previous != null)
                {
                    if (next != null)
                    {
                        next.previousSibling = previous;
                    }
                }
                
                previous.nextSibling = next;
                
                removedSiblings.add(node);
            }
            
            node = previous;
        }
        
        return removedSiblings;
    }
    
    /**
     * Retrieve the siblings of the current with a specific tag.
     * The search doesn't take the deep nodes.
     * @param tag The tag of the researched nodes.
     * @return The nodes with the tag given in parameter.
     */
    public List<XMLNode> getSiblingsByTag(String tag)
    {
        List<XMLNode> siblings = new ArrayList<>();
        
        // return on the first sibling
        XMLNode sibling = this;
        while (sibling.previousSibling != null)
        {
            sibling = sibling.previousSibling;
        }
        
        while (sibling != null)
        {
            if (sibling.tag.equals(tag))
            {
                siblings.add(sibling);
            }
            
            sibling = sibling.nextSibling;
        }
        
        return siblings;
    }
    
    /**
     * Remove sibling at a specific index
     * @param index The index of the target sibling
     * @return The removed node if exist, else <code>null</code>
     */
    public XMLNode removeSiblingAt(int index)
    {
        int i;
        XMLNode node = this.nextSibling;
        XMLNode next, previous;
        
        for (i = 0; i < index; ++i, node = node != null ? node.nextSibling : node);
        next = node.nextSibling;
        previous = node.previousSibling;
        
        if (next != null)
        {
            if (previous != null)
            {
                previous.nextSibling = next;
                if (next != null)
                {
                    next.previousSibling = previous;
                }
            }
        }
        
        return node;
    }
    
    /**
     * Get the cildren of the current node.
     * @return The children of the current nodes.
     */
    public List<XMLNode> getChildren()
    {
        return this.children;
    }
    
    /**
     * Remove the children according to their tag.
     * The deletion doesn't take the deep children.
     * @param tag The tag of the nodes to remove.
     * @return The removed children.
     */
    public List<XMLNode> removeChildrenByTag(String tag)
    {
        List<XMLNode> removedChildren = this.getChildrenByTag(tag);
        this.children.removeAll(removedChildren);
        return removedChildren;
    }
    
    /**
     * Retrieve the children of the current with a specific tag.
     * The search doesn't take the deep nodes.
     * @param tag The tag of the researched nodes.
     * @return The nodes with the tag given in parameter.
     */
    public List<XMLNode> getChildrenByTag(String tag)
    {
        return this.children.stream()
                    .filter(node -> node.tag.equals(tag))
                    .collect(Collectors.toList());
    }
    
    /**
     * Remove the child at a specific index
     * @param index The index of the target child
     * @return The removed child if the index is correct, else <code>null</code>
     */
    public XMLNode removeChildAt(int index)
    {
        if (index < 0 || index > this.children.size())
        {
            return null;
        }
        
        return this.children.remove(index);
    }
    
    /**
     * Get a node by its id.
     * @param id The value of the id
     * @return The node with the id given in parameter.
     */
    public XMLNode getElementById(String id)
    {
        List<XMLNode> availableNodes = this.getElementsByAttribute("id")
                    .stream()
                    .filter(node -> node.attributes.get("id").equals(id))
                    .collect(Collectors.toList());
        return availableNodes.isEmpty() ? null : availableNodes.get(0);
    }
    
    /**
     * Retrieve all of the nodes (children) with a specific tag.
     * @param tag The tag of the researched nodes
     * @return The list of all of the nodes with the specific tag
     */
    public List<XMLNode> getElementsByTag(String tag)
    {
        List<XMLNode> nodes = new ArrayList<>();
        if (this.tag.equals(tag))
        {
            nodes.add(this);
        }
        
        // look at all the siblings
        if (this.nextSibling != null)
        {
            nodes.addAll(this.nextSibling.getElementsByTag(tag));
        }
        
        // search into the children of the current node the elements (deep)
        if (!this.children.isEmpty())
        {
            XMLNode child = this.children.get(0);
            nodes.addAll(child.getElementsByTag(tag));
        }
        
        return nodes;
    }
    
    /**
     * Remove deeply the nodes with the spcecified tag.
     * @param tag The tag for the removed nodes.
     * @return The removed nodes.
     */
    public List<XMLNode> removeNodesByTag(String tag)
    {
        List<XMLNode> removedNodes = new ArrayList<>();
        
        // remove the siblings
        
        return removedNodes;
    }
    
    
    // --------- Attributes methods --------- //
    /**
     * Get the attribute with the specified name of the current node.
     * @param attributeName The name of the wanted attribute.
     * @return The value of the current attribute if exists, else <code>null</code>
     */
    public String getAttribute(String attributeName)
    {
        return this.attributes.get(attributeName);
    }
    
    /**
     * Set a new value of the attribute with specifed name. If the attribute doesn't exist, it will be created.
     * @param attributeName The name of the attribute.
     * @param attributeValue The new value for the attribute.
     */
    public void setAttribute(String attributeName, String attributeValue)
    {
        this.attributes.put(attributeName, attributeValue);
    }
    
    /**
     * Remove the attribute with the specified name.
     * @param attributeName The name of the attribute.
     * @return The value of attribute removed if it exists, else <code>null</code>
     */
    public String removeAttribute(String attributeName)
    {
        return this.attributes.remove(attributeName);
    }
    
    // --------- Parent methods --------- //
    
    /**
     * Get the parent of the current node.
     * @return The parent of the current node.
     */
    public XMLNode getParent()
    {
        return this.parent;
    }
    
    /**
     * Set a new parent for the current node.
     * @param parent  The new parent.
     */
    public void setParent(XMLNode parent)
    {
        this.parent = parent;
        if (parent != null)
        {
            this.level = this.parent.level + 1;
        }
    }
    
    /**
     * Remove the parent of the current node.
     * @return The removed parent of the current node.
     */
    public XMLNode removeParent()
    {
        XMLNode node = this.parent;
        this.parent = null;
        return node;
    }
    
    /**
     * Get a list of nodes with the specified attributeName.
     * @param attributeName The name of the specific attribute.
     * @return The nodes with the specified attributeName.
     */
    public List<XMLNode> getElementsByAttribute(String attributeName)
    {
        List<XMLNode> nodes = new ArrayList<>();

        if (this.attributes.containsKey(attributeName))
        {
            nodes.add(this);
        }
        
         // look at all the siblings
        if (this.nextSibling != null)
        {
            nodes.addAll(this.nextSibling.getElementsByAttribute(attributeName));
        }
        
        // search into the children of the current node the elements (deep)
        if (!this.children.isEmpty())
        {
            XMLNode child = this.children.get(0);
            nodes.addAll(child.getElementsByAttribute(attributeName));
        }
        
        return nodes;
    }
    
    /**
     * Get the text of the current node.
     * @return The text of the current node.
     */
    public String getText()
    {
        return this.text;
    }
    
    /**
     * Set a new value for the text.
     * @param text  The new text.
     */
    public void setText(String text)
    {
        this.text = text;
    }
    
    /**
     * Get the tag of the current node.
     * @return The tag of the current node.
     */
    public String getTag()
    {
        return this.tag;
    }
    
    /**
     * Set a new tag for the current node.
     * @param tag  The value of the new tag.
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    
    /**
     * @return true if the node is an orphan, else false
     */
    public boolean isSelfClosing()
    {
        return this.isSelfClosing;
    }
    
    /**
     * Set a new value for the closing state.
     * @param isSelfClosing  The new value.
     */
    public void setIsSelfClosing(boolean isSelfClosing)
    {
        this.isSelfClosing = isSelfClosing;
    }
    
    @Override
    public String toString()
    {
        return this.toString(0);
    }
    
    public String toString(int tabulation)
    {
        StringBuilder builder = new StringBuilder();
        
        int i;
        for (i = 0; i < tabulation * this.level; ++i)
        {
            builder.append(" ");
        }
        
        builder.append("<")
               .append(this.tag);
        
        // attributes
        i = 0;
        for (Map.Entry<String, String> entry : this.attributes.entrySet())
        {
            builder.append(" ");
            String attributeName = entry.getKey();
            String attributeValue = entry.getValue();
            builder.append(attributeName)
                    .append("='")
                    .append(attributeValue)
                    .append('"');
                
            i++;
        }
        
        if (this.isSelfClosing)
        {
            builder.append("/>\n");
            return builder.toString();
        }
        
        builder.append(">");
        builder.append(this.text);
        
        
        for (XMLNode child : this.children)
        {
            if (tabulation > 0)
            {
                builder.append("\n");
            }
            
            builder.append(child.toString(tabulation));
        }

        if (!this.children.isEmpty())
        {
            builder.append("\n");
            for (i = 0; i < tabulation * this.level; ++i)
            {
                builder.append(" ");
            }
        }
            
        builder.append("</")
               .append(this.tag);
        builder.append(">");

        return builder.toString();
    }
}
