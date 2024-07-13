package cm.nzock.solr;

import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IgnoreWordRepository extends SolrCrudRepository<Dictionnary, String> {

   public Optional<Dictionnary> findById(String id);
}
