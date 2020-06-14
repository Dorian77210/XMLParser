package com.insa.xml;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author Dorian Terbah
 */
public class XMLParser
{
    private static final XMLParser instance = new XMLParser();
        
    private static final char RIGHT_CHEVRON = '>';
    private static final char LEFT_CHEVRON = '<';
    private static final char DELIMITER_SIMPLE_QUOTE = '\'';
    private static final char DELIMITER_DOUBLE_QUOTE = '"';
    
    private String xml;
    private String buffer;
    
    public XMLParser()
    {
        this.xml = "";
        this.buffer = "";
    }
    
    public static final XMLParser getInstance()
    {
        return instance;
    }
    
    public XMLNode parse(String xml)
    {
        this.xml = xml;
        if (this.xml.charAt(1) == '?')
        {
            this.xml.substring(this.xml.lastIndexOf("?>"));
        }
        
        this.xml = this.xml.replace("\n", "");
        
        return this.parse();
    }
    
    private XMLNode parse()
    {
        List<XMLNode> nodes = new ArrayList<>();
        XMLNode currentNode = null, parentNode = null;
        int index, level = 0;
        String tag, text;
        
        // parse all of the nodes
        while (!this.xml.isEmpty())
        {
            this.buffer = "";            
            if (this.xml.charAt(0) == LEFT_CHEVRON)
            {
                if (this.xml.charAt(1) == '/')
                {
                    // end of node
                    index = this.xml.indexOf(RIGHT_CHEVRON);
                    this.xml = this.xml.substring(index + 1);
                    parentNode = (currentNode != null)
                            ? currentNode.getParent()
                            : null;
                    currentNode = parentNode;
                    --level;
                } else if (this.xml.substring(1, 4).equals("!--")) 
                {
                    // for the momemt, don't take comments
                    index = this.xml.indexOf("-->") + 3;
                    if (index == -1)
                    {
                        throw new XMLParsingException("Comment malformed");
                    }
                    
                    this.xml = this.xml.substring(index).trim();
                }
                else
                {
                    // new node
                    currentNode = new XMLNode(false, level);

                    this.xml = this.xml.substring(1);
                    currentNode.level = level++;
                    currentNode.setParent(parentNode);
                    parentNode = currentNode;
                    
                    index = this.xml.indexOf(RIGHT_CHEVRON);
                    int temp = index;
                    if (this.xml.charAt(index - 1) == '/')
                    {
                        // orphan node
                        temp = index - 1;
                        currentNode.setIsSelfClosing(true);
                    }
                    
                    // tag name and attributes
                    this.buffer = this.xml.substring(0, temp);
                    this.xml = this.xml.substring(index + 1).trim();
                    
                    index = this.buffer.indexOf(" ");
                    if (index != -1)
                    {
                        // the node has attributes
                        tag = this.buffer.substring(0, index);
                        this.buffer = this.buffer.substring(index + 1);
                        while (!this.buffer.isEmpty())
                        {
                            Attribute attr = this.nextAttribute();
                            currentNode.setAttribute(attr.name, attr.value);
                        }
                    } else
                    {
                        tag = this.buffer;
                    }
                    
                    nodes.add(currentNode);
                    currentNode.setTag(tag);  
                }
            } else
            {
                // text value
                index = this.xml.indexOf(LEFT_CHEVRON);
                text = this.xml.substring(0, index);
                this.xml = this.xml.substring(index);
                if (currentNode.getText().isEmpty())
                {
                    currentNode.setText(text);
                }
            }
        }
        
        // construction of the tree
        List<XMLNode> currentNodes;
        List<XMLNode> availableSiblings = new ArrayList<>();
        availableSiblings.addAll(nodes);
        
        while (!nodes.isEmpty())
        {
            XMLNode node = nodes.get(0);

            // children
            currentNodes = nodes.stream()
                                .filter(xmlNode -> {
                                    if (xmlNode.equals(node))
                                    {
                                        return false;
                                    }
                                    
                                    if (xmlNode.getParent() != null)
                                    {
                                        if (xmlNode.getParent().equals(node))
                                        {
                                            return true;
                                        }
                                    }
                                    
                                    return false;
                                })
                                .collect(Collectors.toList());
            
            node.appendChildren(currentNodes);
            
            // siblings
            currentNodes = availableSiblings.stream()
                                .filter(xmlNode -> xmlNode.getParent() != null 
                                        ? xmlNode.getParent().equals(node.getParent()) && !xmlNode.equals(node) 
                                        : false
                                )
                                .collect(Collectors.toList());
            
            availableSiblings.removeAll(currentNodes);
            availableSiblings.remove(node);
            
            // initialisation of siblings for currentNode
            currentNode = node;
            
            XMLNode previous, next;
            previous = null;
            
            for (int i = 0; i < currentNodes.size(); ++i)
            {
                next = currentNodes.get(i);
                
                currentNode.setNextSibling(next);
                next.setPreviousSibling(previous);
                
                previous = currentNode;
                currentNode = next;
            }
            
            nodes.remove(node);
        }
        
        parentNode = currentNode.getParent();
        while (parentNode != null)
        {
            currentNode = parentNode;
            parentNode = parentNode.getParent();
        }
        
        return currentNode;
    }
    
    private Attribute nextAttribute()
    {
        String name = "";
        String value = "";
        int index;
        char delimiter = ' ';
        
        index = this.buffer.indexOf("=");
        name = this.buffer.substring(0, index).trim();
        this.buffer = this.buffer.substring(index + 1);
        
        if (this.buffer.charAt(0) == DELIMITER_DOUBLE_QUOTE)
        {
            delimiter = DELIMITER_DOUBLE_QUOTE;
        } else if (this.buffer.charAt(0) == DELIMITER_SIMPLE_QUOTE)
        {
            delimiter = DELIMITER_SIMPLE_QUOTE;
        } else 
        {
            throw new XMLParsingException("The attributes values must begin by a quote");
        }
        
        // pass throught the first delimiter
        this.buffer = this.buffer.substring(1);
        index = this.buffer.indexOf(delimiter);
        value = this.buffer.substring(0, index);
        
        this.buffer = this.buffer.substring(index + 1);
        return new Attribute(name, value);
    }
}
