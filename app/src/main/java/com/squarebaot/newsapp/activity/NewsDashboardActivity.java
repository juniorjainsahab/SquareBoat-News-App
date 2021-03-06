package com.squarebaot.newsapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squarebaot.newsapp.ArticleSearchTextWatcher;
import com.squarebaot.newsapp.R;
import com.squarebaot.newsapp.adapter.NewsArticleDashboardAdapter;
import com.squarebaot.newsapp.model.Article;
import com.squarebaot.newsapp.network.Constant;
import com.squarebaot.newsapp.network.ServiceBuilder;
import com.squarebaot.newsapp.network.services.FetchNewsArticle;
import com.squarebaot.newsapp.presenter.MainActivityPresenter;
import com.squarebaot.newsapp.view.NewsDashboardView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.squarebaot.newsapp.R.id.location_menu_item;
import static com.squarebaot.newsapp.R.id.no_result;
import static com.squarebaot.newsapp.R.id.progress_bar;
import static com.squarebaot.newsapp.R.id.recyclerview;
import static com.squarebaot.newsapp.R.id.search;
import static com.squarebaot.newsapp.R.id.source_filter;
import static com.squarebaot.newsapp.SourceFilter.selectedSources;

public class NewsDashboardActivity extends BaseActivity implements NewsDashboardView {

    @BindView(recyclerview)
    RecyclerView recyclerView;

    @BindView(progress_bar)
    ProgressBar progressBar;

    @BindView(no_result)
    ConstraintLayout emptyResultLayout;

    @BindView(search)
    EditText searchEditText;

    MainActivityPresenter mainActivityPresenter;
    private List<Article> articles;
    private NewsArticleDashboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_dashboard);
        ButterKnife.bind(this);

        fetchTopHeadlinesFor("IN");
    }

    private void fetchTopHeadlinesFor(String countryISOCode) {
        mainActivityPresenter = new MainActivityPresenter(
                ServiceBuilder.build(FetchNewsArticle.class), this);
        mainActivityPresenter.fetchTopHeadlines(countryISOCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case location_menu_item:
                openCountryBottomSheetFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void articlesFetchSuccessful(List<Article> articles) {
        this.articles = articles;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsArticleDashboardAdapter(articles, this);
        recyclerView.setAdapter(adapter);
        searchEditText.addTextChangedListener(new ArticleSearchTextWatcher(articles, adapter, this));
    }

    @Override
    public void articlesFetchFail() {
        Toast.makeText(getApplicationContext(), "Request Fail", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(GONE);
    }

    @Override
    public void onArticleSelect(View listItemView, List<Article> articles) {
        Intent intent = new Intent(this, ArticleBrowseActivity.class);
        intent.putExtra(Constant.ARTICLE, articles.get(recyclerView.getChildLayoutPosition(listItemView)));
        startActivity(intent);
    }

    @Override
    public void onArticleListIsEmpty() {
        emptyResultLayout.setVisibility(VISIBLE);
        recyclerView.setVisibility(GONE);
    }

    @Override
    public void onArticleListIsNotEmpty() {
        emptyResultLayout.setVisibility(GONE);
        recyclerView.setVisibility(VISIBLE);
    }

    @Override
    public void updateListBySources() {
        if (selectedSources.isEmpty()) {
            onArticleListIsNotEmpty();
            adapter.updateDataSet(this.articles);
            return;
        }
        List<Article> filteredBySource = new ArrayList<>();
        for (Article article : this.articles) {
            if (selectedSources.contains(article.getSource().getName()))
                filteredBySource.add(article);
        }
        if (filteredBySource.isEmpty()) {
            onArticleListIsEmpty();
            return;
        }
        onArticleListIsNotEmpty();
        adapter.updateDataSet(filteredBySource);
    }

    @Override
    public void onCountrySelect(String countryISOCode) {
        fetchTopHeadlinesFor(countryISOCode);
    }

    @OnClick(source_filter)
    public void onSourceFilterButtonClick() {
        SourceFilterBottomSheetDialogFragment sourcesDialog = new SourceFilterBottomSheetDialogFragment(this);
        sourcesDialog.show(getSupportFragmentManager(), "sourceFragment");
    }

    private void openCountryBottomSheetFragment() {
        CountryDialogFragment countryDialog = new CountryDialogFragment(this);
        countryDialog.show(getSupportFragmentManager(), "countryFragment");
    }

}