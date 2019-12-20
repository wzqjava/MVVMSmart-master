package com.wzq.sample.ui.network;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wzq.mvvmsmart.base.BaseFragment;
import com.wzq.mvvmsmart.rv_adapter.BaseViewAdapter;
import com.wzq.mvvmsmart.rv_adapter.BindingViewHolder;
import com.wzq.mvvmsmart.rv_adapter.SingleTypeAdapter;
import com.wzq.mvvmsmart.utils.KLog;
import com.wzq.mvvmsmart.utils.MaterialDialogUtils;
import com.wzq.mvvmsmart.utils.ToastUtils;
import com.wzq.sample.R;
import com.wzq.sample.app.AppViewModelFactory;
import com.wzq.sample.databinding.FragmentNetworkBinding;
import com.wzq.sample.entity.Bean2;
import com.wzq.sample.entity.DemoBean;

import java.util.List;

/**
 * 王志强 2019/12/20
 * 网络请求列表界面
 * https://www.oschina.net/action/apiv2/banner?catalog=1
 *
 * 分页
 * 网络返回状态封装,livedata事件儿包装器
 * GithubBrowserSample  (NetworkBoundResource), Google AAC 架构中的加载网络or DB的策略
 */
public class NetWorkFragment extends BaseFragment<FragmentNetworkBinding, NetWorkViewModel> {
    @Override
    public void initParam() {
        super.initParam();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public int initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return R.layout.fragment_network;
    }

    @Override
    public int initVariableId() {
        return com.wzq.sample.BR.viewModel;
    }

    @Override
    public NetWorkViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用NetWorkViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getActivity().getApplication());
        return ViewModelProviders.of(this, factory).get(NetWorkViewModel.class);
    }

    @Override
    public void initData() {
        viewModel.requestNetWork();        //请求网络数据
        initRecyclerView();
    }

    private void initRecyclerView() {
        SingleTypeAdapter singleTypeAdapter = new SingleTypeAdapter(getActivity(), R.layout.item_single);
        binding.setAdapter(singleTypeAdapter);
        singleTypeAdapter.setDecorator(new DemoAdapterDecorator());
        singleTypeAdapter.setPresenter(new DemoAdapterPresenter());
        binding.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.setAdapter(singleTypeAdapter);

        MutableLiveData<List<DemoBean.ItemsEntity>> liveData = viewModel.getLiveData();
        liveData.observe(this, itemsEntities -> {
            KLog.i("NetWorkFragment", "livedata数据改变,listBeans.size()::" + itemsEntities.size());
            singleTypeAdapter.set(itemsEntities);
        });

    }

    @Override
    public void initViewObservable() {

        //监听下拉刷新完成
        viewModel.uc.finishRefreshing.observe(NetWorkFragment.this, new Observer<Object>() {
            @Override
            public void onChanged(@Nullable Object o) {
                binding.refreshLayout.finishRefresh();    //结束刷新
            }
        });
        //监听上拉加载完成
        viewModel.uc.finishLoadMore.observe(NetWorkFragment.this, new Observer<Object>() {
            @Override
            public void onChanged(@Nullable Object o) {
                binding.refreshLayout.finishLoadMore();   //结束刷新
            }
        });
        //监听删除条目
        viewModel.deleteItemLiveData.observe(this, new Observer<NetWorkItemViewModel>() {
            @Override
            public void onChanged(@Nullable final NetWorkItemViewModel netWorkItemViewModel) {
                int index = viewModel.getItemPosition(netWorkItemViewModel);
                //删除选择对话框
                MaterialDialogUtils.showBasicDialog(getContext(), "提示", "是否删除【" + netWorkItemViewModel.entity.get().getName() + "】？ position：" + index)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ToastUtils.showShort("取消");
                            }
                        }).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        viewModel.deleteItem(netWorkItemViewModel);
                    }
                }).show();
            }
        });
    }


    public class DemoAdapterPresenter implements BaseViewAdapter.Presenter {
        public void onItemClick(DemoBean.ItemsEntity student) {
            ToastUtils.showShort(student.getName());
        }

        public void onItemClick(Bean2 model) {
            Toast.makeText(getActivity(), model.name, Toast.LENGTH_SHORT).show();
        }
    }

    public class DemoAdapterDecorator implements BaseViewAdapter.Decorator {

        @Override
        public void decorator(BindingViewHolder holder, int position, int viewType) {
            // you may do something according to position or view type
        }
    }
}