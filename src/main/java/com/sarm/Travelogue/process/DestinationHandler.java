/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this defaultTemplate file, choose Tools | Templates
 * and open the defaultTemplate in the editor.
 */
package com.sarm.Travelogue.process;

import com.sarm.Travelogue.common.PostMarshalUtilities;
import com.sarm.Travelogue.model.Destination;
import com.sarm.Travelogue.model.Node;
import com.sarm.Travelogue.model.Taxonomy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * This class is the recursive action that will be added to the ForkJoinPool to
 * It will add itself recursively to for each destination and create Html files
 * for each destination.
 *
 * @author sarm
 */
class DestinationHandler extends RecursiveAction {

    org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DestinationHandler.class);

    int sizeOfDestination;
    List<Destination> destinations;
    Taxonomy taxonomy;
    String targetLocation;

    public DestinationHandler(List<Destination> destinations, Taxonomy taxonomy, int size, String targetLocation) {
        this.destinations = destinations;
        this.taxonomy = taxonomy;
        this.sizeOfDestination = size;
        this.targetLocation = targetLocation;
    }

    /**
     * This is the implementation of the abstract method in RecursiveAction.
     * Once the invoke method is called the compute method is triggered to be
     * executed.
     */
    @Override
    protected void compute() {

        List<RecursiveAction> actions = new ArrayList<>();
        if (destinations.size() == 1) {
            try {
                Node destinationNode = PostMarshalUtilities.getNodeByDestinationTitle(taxonomy, destinations.get(0).getTitle());

                PostMarshalUtilities.createHtmlFromTemplate(destinations.get(0), destinationNode, targetLocation);
            } catch (IOException | NullPointerException/* | NodeNotFoundException*/ ex) {
                ex.printStackTrace();
                logger.error(ex + "     Exception in creating an html template for the destination " + destinations.get(0).getTitle(), ex);
            }

        } else {
            for (int i = 0; i < destinations.size(); i++) {
                List<Destination> subDestination = new ArrayList<>();
                subDestination.add(destinations.get(i));
                actions.add(new DestinationHandler(subDestination, taxonomy, i, targetLocation));
                invokeAll(actions);

            }
        }

    }

}
