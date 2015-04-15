/*
 * The Gemma project
 * 
 * Copyright (c) 2012 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.search;

import ubic.gemma.model.common.auditAndSecurity.UserQuery;
import ubic.gemma.model.common.search.SearchSettings;
import ubic.gemma.model.common.search.SearchSettingsValueObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author paul
 * @version $Id: SearchService.java,v 1.169 2013/04/09 21:33:35 cmcdonald Exp $
 */
public interface SearchService {

    /**
     * The results are sorted in order of decreasing score, organized by class. The following objects can be searched
     * for, depending on the configuration of the input object.
     * <ul>
     * <li>Genes
     * <li>ExpressionExperiments
     * <li>CompositeSequences (probes)
     * <li>ArrayDesigns (platforms)
     * <li>Characteristics (e.g., Ontology annotations)
     * <li>BioSequences
     * <li>BibliographicReferences (articles)
     * </ul>
     *
     * @param settings
     * @return Map of Class to SearchResults. The results are already filtered for security considerations.
     */
    public Map<Class<?>, List<SearchResult>> ajaxSearch( SearchSettingsValueObject settings );


    /**
     * The results are sorted in order of decreasing score, organized by class. The following objects can be searched
     * for, depending on the configuration of the input object.
     * <ul>
     * <li>Genes
     * <li>ExpressionExperiments
     * <li>CompositeSequences (probes)
     * <li>ArrayDesigns (platforms)
     * <li>Characteristics (e.g., Ontology annotations)
     * <li>BioSequences
     * <li>BibliographicReferences (articles)
     * </ul>
     * 
     * @param settings
     * @return Map of Class to SearchResults. The results are already filtered for security considerations.
     */
    public Map<Class<?>, List<SearchResult>> search( SearchSettings settings );
    
    
    /**
     * This speedSearch method is probably unneccessary right now considering we only call from geneSearch, just putting it in
     * because we probably want to use something like this on the general search page
     * 
     * @param settings
     * 
     * @return
     * @see SearchService.search(SearchSettings settings)
     */
    public Map<Class<?>, List<SearchResult>> speedSearch( SearchSettings settings );

    /**
     * Makes an attempt at determining of the query term is a valid URI from an Ontology in Gemma or a Gene URI (a GENE
     * URI is in the form: http://purl.org/commons/record/ncbi_gene/20655 (but is not a valid ontology loaded in gemma)
     * ). If so then searches for objects that have been tagged with that term or any of that terms children. If not a
     * URI then proceeds with the generalSearch.
     * 
     * @param settings
     * @param fillObjects If false, the entities will not be filled in inside the searchsettings; instead, they will be
     *        nulled (for security purposes). You can then use the id and Class stored in the SearchSettings to load the
     *        entities at your leisure. If true, the entities are loaded in the usual secure fashion. Setting this to
     *        false can be an optimization if all you need is the id.
     * @param webSpeedSearch If true, the search will be faster but the results may not be as broad as when this is false. 
     * 		  Set to true for frontend combo boxes like the gene combo    
     * @return
     * @see SearchService.search(SearchSettings settings)
     */
    public Map<Class<?>, List<SearchResult>> search( SearchSettings settings, boolean fillObjects, boolean webSpeedSearch );

    /**
     * @param query if empty, all experiments for the taxon are returned; otherwise, we use the search facility.
     * @param taxonId required.
     * @return Collection of ids.
     */
    public Collection<Long> searchExpressionExperiments( String query, Long taxonId );

    /**
     * convenience method to return only search results from one class
     * 
     * @param settings
     * @param resultClass
     * @return
     */
    public List<?> search( SearchSettings settings, Class<?> resultClass );
    
    public Map<Class<?>, List<SearchResult>> searchForNewlyCreatedUserQueryResults( UserQuery uq );

}