package com.lex.simplequest.presentation.screen.launcher

import com.lex.simplequest.presentation.base.BaseMvpPresenter

class LauncherActivityPresenter(
    router: LauncherRouter
) : BaseMvpPresenter<LauncherActivityContract.Ui, LauncherActivityContract.Presenter.State, LauncherRouter>(router),
    LauncherActivityContract.Presenter