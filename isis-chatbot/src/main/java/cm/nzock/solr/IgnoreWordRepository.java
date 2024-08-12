package cm.nzock.solr;

import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IgnoreWordRepository extends SolrCrudRepository<Dictionnary, String> {

   public static final String IGNORE_STR="ignore";
   public static final String KEYWORD_STR="keyword";

   public Optional<Dictionnary> findById(String id);

   @Query("id:?0 AND type:?1")
   public Optional<Dictionnary> findByIdAndType(final String id, final String type);
}
