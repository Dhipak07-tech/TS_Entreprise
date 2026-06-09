import { useEffect, useState } from 'react';
import { kbService } from '../../services/dataService';
import {
  BookOpen, Search, FolderOpen, Star, Clock, Eye
} from 'lucide-react';
import './Knowledge.css';

export default function KnowledgeBasePage() {
  const [articles, setArticles] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    loadArticles();
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const data = await kbService.getCategories();
      setCategories(data);
    } catch (err) {
      console.error('Failed to load KB categories:', err);
    }
  };

  const loadArticles = async () => {
    setLoading(true);
    try {
      const data = await kbService.getArticles(0, 20, 'PUBLISHED');
      setArticles(data.content);
    } catch (err) {
      console.error('Failed to load KB articles:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery) return loadArticles();
    
    setLoading(true);
    try {
      const data = await kbService.searchArticles(searchQuery);
      setArticles(data.content);
    } catch (err) {
      console.error('Search failed:', err);
    } finally {
      setLoading(false);
    }
  };

  const filteredArticles = selectedCategoryId
    ? articles.filter(article => article.category && article.category.id === selectedCategoryId)
    : articles;

  return (
    <div className="kb-page animate-fade-in">
      {/* Hero Banner */}
      <div className="kb-hero">
        <div className="kb-hero-content">
          <h1>How can we help you today?</h1>
          <p>Search our knowledge base for answers to common questions and issues.</p>
          
          <form onSubmit={handleSearch} className="kb-search-bar">
            <Search className="kb-search-icon" size={20} />
            <input 
              type="text" 
              placeholder="Search articles, error codes, tutorials..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button type="submit" className="btn btn-primary">Search</button>
          </form>
        </div>
        <div className="kb-hero-bg"></div>
      </div>

      <div className="kb-content-grid">
        {/* Categories Sidebar */}
        <div className="kb-categories card">
          <h3 className="kb-section-title">Categories</h3>
          <ul className="kb-category-list">
             <li 
               className={selectedCategoryId === null ? 'active' : ''}
               onClick={() => setSelectedCategoryId(null)}
               style={{ cursor: 'pointer' }}
             >
               <FolderOpen size={16}/> All Articles
             </li>
             {categories.map(category => (
               <li 
                 key={category.id}
                 className={selectedCategoryId === category.id ? 'active' : ''}
                 onClick={() => setSelectedCategoryId(category.id)}
                 style={{ cursor: 'pointer' }}
               >
                 <FolderOpen size={16}/> {category.name}
               </li>
             ))}
          </ul>
        </div>

        {/* Article Feed */}
        <div className="kb-articles">
          <div className="kb-header-row">
            <h3 className="kb-section-title">
              {searchQuery ? `Search Results for "${searchQuery}"` : 'Recent Articles'}
            </h3>
          </div>

          <div className="kb-article-grid">
            {loading ? (
               <div className="skeleton card" style={{height: 150}}></div>
            ) : filteredArticles.length === 0 ? (
               <div className="card empty-state">
                  <BookOpen size={48} className="empty-icon" />
                  <p>No articles found.</p>
               </div>
            ) : (
              filteredArticles.map(article => (
                <div key={article.id} className="kb-article-card card">
                  {article.isPinned && <div className="pinned-badge"><Star size={12}/> Pinned</div>}
                  <h4 className="article-title">{article.title}</h4>
                  <p className="article-excerpt">
                    {article.content ? article.content.substring(0, 120) + '...' : 'No description available.'}
                  </p>
                  <div className="article-meta">
                     <span><Clock size={14}/> {new Date(article.publishedAt || article.createdAt).toLocaleDateString()}</span>
                     <span><Eye size={14}/> {article.viewCount} views</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
