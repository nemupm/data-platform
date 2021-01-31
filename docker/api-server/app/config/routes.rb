Rails.application.routes.draw do
  namespace 'api' do
    namespace 'v1' do
      get :word_cooccurrences, to: 'word_cooccurrences#show'
    end
  end
end
